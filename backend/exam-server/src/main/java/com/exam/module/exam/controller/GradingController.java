package com.exam.module.exam.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.exam.common.result.Result;
import com.exam.module.exam.entity.*;
import com.exam.module.exam.mapper.*;
import com.exam.module.paper.entity.PaperQuestion;
import com.exam.module.paper.mapper.PaperQuestionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "阅卷管理", description = "主观题阅卷、成绩发布")
@RestController
@RequestMapping("/api/v1/admin/grading")
@RequiredArgsConstructor
public class GradingController {

    private final ExamStudentMapper examStudentMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final ExamMapper examMapper;
    private final PaperQuestionMapper paperQuestionMapper;

    @Operation(summary = "待阅卷/已阅卷列表")
    @GetMapping("/list")
    public Result<Map<String,Object>> list(@RequestParam(required = false) Long examId,
                                            @RequestParam(defaultValue = "0") int status, // 0待阅 1已阅
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int pageSize) {
        var qw = Wrappers.lambdaQuery(ExamStudent.class)
                .eq(ExamStudent::getStatus, 2) // 已交卷
                .eq(status == 1, ExamStudent::getGradingStatus, 1)
                .eq(status == 0, ExamStudent::getGradingStatus, 0)
                .eq(examId != null, ExamStudent::getExamId, examId)
                .orderByDesc(ExamStudent::getSubmitTime);
        var mp = examStudentMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize), qw);

        List<Map<String,Object>> records = mp.getRecords().stream().map(s -> {
            Exam e = examMapper.selectById(s.getExamId());
            Map<String,Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("examId", s.getExamId());
            m.put("examTitle", e != null ? e.getTitle() : "");
            m.put("userId", s.getUserId());
            m.put("submitTime", s.getSubmitTime());
            m.put("objectiveScore", s.getObjectiveScore());
            m.put("totalScore", s.getTotalScore());
            m.put("gradingStatus", s.getGradingStatus());
            return m;
        }).collect(Collectors.toList());

        Map<String,Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", mp.getTotal());
        return Result.ok(result);
    }

    @Operation(summary = "查看考生答卷（阅卷用）")
    @GetMapping("/{examStudentId}")
    public Result<Map<String,Object>> getAnswers(@PathVariable Long examStudentId) {
        ExamStudent s = examStudentMapper.selectById(examStudentId);
        if (s == null) return Result.fail(404, "记录不存在");

        Exam exam = examMapper.selectById(s.getExamId());
        List<AnswerRecord> answers = answerRecordMapper.selectList(
                Wrappers.lambdaQuery(AnswerRecord.class).eq(AnswerRecord::getExamStudentId, examStudentId));
        List<PaperQuestion> pqs = paperQuestionMapper.selectList(
                Wrappers.lambdaQuery(PaperQuestion.class).eq(PaperQuestion::getPaperId, exam.getPaperId()).orderByAsc(PaperQuestion::getSortOrder));

        List<Map<String,Object>> answerList = pqs.stream().map(pq -> {
            AnswerRecord ar = answers.stream().filter(a -> a.getPaperQuestionId().equals(pq.getId())).findFirst().orElse(null);
            Map<String,Object> m = new HashMap<>();
            m.put("paperQuestionId", pq.getId());
            m.put("questionType", pq.getQuestionType());
            m.put("title", pq.getTitleSnapshot());
            m.put("answerSnapshot", pq.getAnswerSnapshot());
            m.put("score", pq.getScore());
            m.put("userAnswer", ar != null ? ar.getUserAnswer() : null);
            m.put("isCorrect", ar != null ? ar.getIsCorrect() : null);
            m.put("gradedScore", ar != null ? ar.getScore() : null);
            m.put("graderComment", ar != null ? ar.getGraderComment() : null);
            return m;
        }).collect(Collectors.toList());

        Map<String,Object> result = new HashMap<>();
        result.put("examStudent", s);
        result.put("examTitle", exam.getTitle());
        result.put("answers", answerList);
        return Result.ok(result);
    }

    @Operation(summary = "提交主观题评分")
    @PostMapping("/{examStudentId}/score")
    @Transactional
    public Result<Void> submitScore(@PathVariable Long examStudentId, @RequestBody Map<String,Object> data) {
        ExamStudent s = examStudentMapper.selectById(examStudentId);
        if (s == null) return Result.fail(404, "记录不存在");

        @SuppressWarnings("unchecked")
        List<Map<String,Object>> scores = (List<Map<String,Object>>) data.get("scores");
        if (scores != null) {
            int subTotal = 0;
            for (Map<String,Object> sc : scores) {
                Long pqId = Long.valueOf(sc.get("paperQuestionId").toString());
                int score = Integer.valueOf(sc.get("score").toString());
                String comment = sc.get("comment") != null ? sc.get("comment").toString() : null;

                AnswerRecord ar = answerRecordMapper.selectOne(
                        Wrappers.lambdaQuery(AnswerRecord.class)
                                .eq(AnswerRecord::getExamStudentId, examStudentId)
                                .eq(AnswerRecord::getPaperQuestionId, pqId));
                if (ar != null) {
                    ar.setScore(score);
                    ar.setGraderComment(comment);
                    ar.setGradedTime(LocalDateTime.now());
                    answerRecordMapper.updateById(ar);
                    subTotal += score;
                }
            }
            s.setSubjectiveScore(subTotal);
            s.setTotalScore(s.getObjectiveScore() + subTotal);
            Exam exam = examMapper.selectById(s.getExamId());
            s.setIsPassed(s.getTotalScore() >= (exam.getPassScore() != null ? exam.getPassScore() : 60) ? 1 : 0);
        }

        // 发布成绩
        if (Boolean.TRUE.equals(data.get("publish"))) {
            s.setGradingStatus(1);
            s.setStatus(3);
        }
        examStudentMapper.updateById(s);
        return Result.ok();
    }

    @Operation(summary = "发布成绩")
    @PostMapping("/{examId}/publish")
    public Result<Void> publishScore(@PathVariable Long examId) {
        List<ExamStudent> students = examStudentMapper.selectList(
                Wrappers.lambdaQuery(ExamStudent.class).eq(ExamStudent::getExamId, examId).eq(ExamStudent::getStatus, 2));
        for (ExamStudent s : students) {
            s.setStatus(3);
            s.setGradingStatus(1);
            examStudentMapper.updateById(s);
        }
        return Result.ok();
    }
}
