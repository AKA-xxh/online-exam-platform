package com.exam.module.statistics.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.exam.common.result.Result;
import com.exam.module.exam.entity.*;
import com.exam.module.exam.mapper.*;
import com.exam.module.course.mapper.CourseMapper;
import com.exam.module.user.mapper.SysUserMapper;
import com.exam.module.paper.entity.PaperQuestion;
import com.exam.module.paper.mapper.PaperQuestionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "统计分析", description = "Dashboard 指标、考试统计、成绩分布")
@RestController
@RequestMapping("/api/v1/admin/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final SysUserMapper userMapper;
    private final CourseMapper courseMapper;
    private final ExamMapper examMapper;
    private final ExamStudentMapper examStudentMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final PaperQuestionMapper paperQuestionMapper;

    @Operation(summary = "Dashboard 数据看板")
    @GetMapping("/dashboard")
    public Result<Map<String,Object>> dashboard() {
        Map<String,Object> d = new HashMap<>();
        d.put("totalStudents", userMapper.selectCount(null));
        d.put("totalCourses", courseMapper.selectCount(null));
        d.put("totalExams", examMapper.selectCount(null));
        d.put("inProgressExams", examMapper.selectCount(Wrappers.lambdaQuery(Exam.class).eq(Exam::getStatus, 1)));
        d.put("todayFinished", examStudentMapper.selectCount(
                Wrappers.lambdaQuery(ExamStudent.class).ge(ExamStudent::getSubmitTime, java.time.LocalDate.now().atStartOfDay())));
        return Result.ok(d);
    }

    @Operation(summary = "考试维度统计")
    @GetMapping("/exam/{examId}")
    public Result<Map<String,Object>> examStats(@PathVariable Long examId) {
        List<ExamStudent> students = examStudentMapper.selectList(
                Wrappers.lambdaQuery(ExamStudent.class).eq(ExamStudent::getExamId, examId).ge(ExamStudent::getStatus, 2));
        if (students.isEmpty()) return Result.ok(new HashMap<>());

        List<Integer> scores = students.stream().map(ExamStudent::getTotalScore).filter(Objects::nonNull).collect(Collectors.toList());
        if (scores.isEmpty()) return Result.ok(new HashMap<>());

        IntSummaryStatistics summary = scores.stream().mapToInt(Integer::intValue).summaryStatistics();
        long passCount = students.stream().filter(s -> s.getIsPassed() != null && s.getIsPassed() == 1).count();

        // 分数段分布
        Map<String,Long> distribution = new LinkedHashMap<>();
        for (String range : new String[]{"0-59", "60-69", "70-79", "80-89", "90-100"}) {
            int low = Integer.parseInt(range.split("-")[0]);
            int high = Integer.parseInt(range.split("-")[1]);
            distribution.put(range, scores.stream().filter(s -> s >= low && s <= high).count());
        }

        Map<String,Object> result = new HashMap<>();
        result.put("totalStudents", students.size());
        result.put("actualStudents", students.stream().filter(s -> s.getStatus() >= 2).count());
        result.put("avgScore", summary.getAverage());
        result.put("maxScore", summary.getMax());
        result.put("minScore", summary.getMin());
        result.put("passCount", passCount);
        result.put("passRate", students.size() > 0 ? passCount * 100.0 / students.size() : 0);
        result.put("scoreDistribution", distribution);
        return Result.ok(result);
    }

    @Operation(summary = "题目维度正确率")
    @GetMapping("/questions/{examId}")
    public Result<List<Map<String,Object>>> questionStats(@PathVariable Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) return Result.ok(Collections.emptyList());
        List<PaperQuestion> pqs = paperQuestionMapper.selectList(
                Wrappers.lambdaQuery(PaperQuestion.class).eq(PaperQuestion::getPaperId, exam.getPaperId()).orderByAsc(PaperQuestion::getSortOrder));
        List<AnswerRecord> answers = answerRecordMapper.selectList(
                Wrappers.lambdaQuery(AnswerRecord.class).eq(AnswerRecord::getExamId, examId));

        return Result.ok(pqs.stream().map(pq -> {
            List<AnswerRecord> related = answers.stream().filter(a -> a.getPaperQuestionId().equals(pq.getId())).collect(Collectors.toList());
            long correct = related.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect() == 1).count();
            Map<String,Object> m = new HashMap<>();
            m.put("paperQuestionId", pq.getId());
            m.put("title", pq.getTitleSnapshot() != null && pq.getTitleSnapshot().length() > 50 ? pq.getTitleSnapshot().substring(0, 50) + "..." : pq.getTitleSnapshot());
            m.put("questionType", pq.getQuestionType());
            m.put("score", pq.getScore());
            m.put("totalAnswers", related.size());
            m.put("correctCount", correct);
            m.put("correctRate", related.isEmpty() ? 0 : correct * 100.0 / related.size());
            return m;
        }).collect(Collectors.toList()));
    }

    @Operation(summary = "成绩列表")
    @GetMapping("/scores")
    public Result<Map<String,Object>> scores(@RequestParam(required = false) Long examId,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int pageSize) {
        var qw = Wrappers.lambdaQuery(ExamStudent.class)
                .eq(examId != null, ExamStudent::getExamId, examId)
                .ge(ExamStudent::getStatus, 2)
                .orderByDesc(ExamStudent::getTotalScore);
        var mp = examStudentMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize), qw);
        List<Map<String,Object>> records = mp.getRecords().stream().map(s -> {
            Exam e = examMapper.selectById(s.getExamId());
            Map<String,Object> m = new HashMap<>();
            m.put("id", s.getId()); m.put("examId", s.getExamId()); m.put("examTitle", e != null ? e.getTitle() : "");
            m.put("userId", s.getUserId()); m.put("totalScore", s.getTotalScore());
            m.put("objectiveScore", s.getObjectiveScore()); m.put("subjectiveScore", s.getSubjectiveScore());
            m.put("isPassed", s.getIsPassed()); m.put("usedTime", s.getUsedTime());
            m.put("submitTime", s.getSubmitTime());
            return m;
        }).collect(Collectors.toList());
        Map<String,Object> result = new HashMap<>();
        result.put("records", records); result.put("total", mp.getTotal());
        return Result.ok(result);
    }
}
