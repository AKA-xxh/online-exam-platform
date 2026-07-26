package com.exam.module.exam.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.exception.BusinessException;
import com.exam.common.result.ErrorCode;
import com.exam.common.result.PageResult;
import com.exam.module.exam.entity.*;
import com.exam.module.exam.mapper.*;
import com.exam.module.paper.entity.*;
import com.exam.module.paper.mapper.*;
import com.exam.module.question.entity.QuestionOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamMapper examMapper;
    private final ExamStudentMapper examStudentMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final ExamPaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;

    // ============ 考试管理 ============
    public PageResult<Map<String,Object>> getPage(String keyword, Integer status, int page, int pageSize) {
        LambdaQueryWrapper<Exam> qw = Wrappers.lambdaQuery(Exam.class)
                .eq(status != null, Exam::getStatus, status)
                .like(keyword != null, Exam::getTitle, keyword)
                .orderByDesc(Exam::getCreateTime);
        Page<Exam> mp = examMapper.selectPage(new Page<>(page, pageSize), qw);
        List<Map<String,Object>> recs = mp.getRecords().stream().map(e -> {
            Map<String,Object> m = new HashMap<>();
            m.put("id", e.getId()); m.put("title", e.getTitle()); m.put("status", e.getStatus());
            m.put("startTime", e.getStartTime()); m.put("endTime", e.getEndTime());
            m.put("duration", e.getDuration());
            Long cnt = examStudentMapper.selectCount(Wrappers.lambdaQuery(ExamStudent.class).eq(ExamStudent::getExamId, e.getId()));
            m.put("studentCount", cnt);
            return m;
        }).collect(Collectors.toList());
        return new PageResult<>(recs, mp.getTotal(), page, pageSize, mp.getPages());
    }

    @Transactional
    public Exam publish(Map<String,Object> data, Long userId) {
        Exam exam = new Exam();
        exam.setPaperId(Long.valueOf(data.get("paperId").toString()));
        exam.setTitle(data.get("title") != null ? data.get("title").toString() : "考试");
        exam.setDescription(data.get("description") != null ? data.get("description").toString() : null);
        // 兼容 "yyyy-MM-dd HH:mm:ss" 和 "yyyy-MM-ddTHH:mm:ss" 两种格式
        exam.setStartTime(LocalDateTime.parse(data.get("startTime").toString().replace(" ", "T")));
        exam.setEndTime(LocalDateTime.parse(data.get("endTime").toString().replace(" ", "T")));
        exam.setDuration(Integer.valueOf(data.get("duration").toString()));
        exam.setStudentScope(data.get("studentScope") != null ? Integer.valueOf(data.get("studentScope").toString()) : 1);
        if (data.get("studentIds") != null) exam.setStudentIds(JSONUtil.toJsonStr(data.get("studentIds")));
        exam.setPassScore(data.get("passScore") != null ? Integer.valueOf(data.get("passScore").toString()) : 60);
        // 根据发布时间自动设置考试状态
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exam.getStartTime())) exam.setStatus(0);      // 未开始
        else if (now.isBefore(exam.getEndTime())) exam.setStatus(1);   // 进行中
        else exam.setStatus(2);                                         // 已结束
        exam.setCreateBy(userId);
        examMapper.insert(exam);
        return exam;
    }

    public void cancel(Long id) {
        Exam e = examMapper.selectById(id);
        if (e == null) throw new BusinessException(ErrorCode.EXAM_NOT_FOUND);
        e.setStatus(3); examMapper.updateById(e);
    }

    public Map<String,Object> getMonitor(Long examId) {
        List<ExamStudent> students = examStudentMapper.selectList(
                Wrappers.lambdaQuery(ExamStudent.class).eq(ExamStudent::getExamId, examId));
        List<Map<String,Object>> list = students.stream().map(s -> {
            Map<String,Object> m = new HashMap<>();
            m.put("userId", s.getUserId()); m.put("status", s.getStatus());
            m.put("startTime", s.getStartTime()); m.put("usedTime", s.getUsedTime());
            m.put("screenSwitches", s.getScreenSwitches()); m.put("isCheated", s.getIsCheated());
            return m;
        }).collect(Collectors.toList());
        Map<String,Object> result = new HashMap<>();
        result.put("students", list);
        result.put("total", students.size());
        result.put("inProgress", students.stream().filter(s -> s.getStatus() == 1).count());
        result.put("submitted", students.stream().filter(s -> s.getStatus() >= 2).count());
        return result;
    }

    @Transactional
    public void forceSubmit(Long examId, Long userId) {
        ExamStudent s = getStudentRecord(examId, userId);
        if (s.getStatus() == 1) {
            gradeAndSubmit(s);
        }
    }

    // ============ 学员考试 ============
    public List<Map<String,Object>> getMyExams(Long userId) {
        // 1. 查找已参加的考试记录
        List<ExamStudent> records = examStudentMapper.selectList(
                Wrappers.lambdaQuery(ExamStudent.class).eq(ExamStudent::getUserId, userId));
        Set<Long> joinedExamIds = records.stream().map(ExamStudent::getExamId).collect(Collectors.toSet());

        // 2. 查找全部学员可见的考试
        List<Exam> publicExams = examMapper.selectList(
                Wrappers.lambdaQuery(Exam.class).eq(Exam::getStudentScope, 1).ne(Exam::getStatus, 3));

        // 3. 查找指定学员的考试（studentIds JSON 中包含该 userId）
        List<Exam> allExams = examMapper.selectList(
                Wrappers.lambdaQuery(Exam.class).eq(Exam::getStudentScope, 2).ne(Exam::getStatus, 3));
        List<Exam> targetedExams = allExams.stream()
                .filter(e -> e.getStudentIds() != null && e.getStudentIds().contains(userId.toString()))
                .collect(Collectors.toList());

        // 4. 合并
        Set<Long> resultIds = new HashSet<>(joinedExamIds);
        resultIds.addAll(publicExams.stream().map(Exam::getId).collect(Collectors.toList()));
        resultIds.addAll(targetedExams.stream().map(Exam::getId).collect(Collectors.toList()));

        if (resultIds.isEmpty()) return Collections.emptyList();
        List<Exam> exams = examMapper.selectBatchIds(resultIds);
        return exams.stream().map(e -> {
            ExamStudent s = records.stream().filter(r -> r.getExamId().equals(e.getId())).findFirst().orElse(null);
            Map<String,Object> m = new HashMap<>();
            m.put("examId", e.getId()); m.put("title", e.getTitle());
            m.put("startTime", e.getStartTime()); m.put("endTime", e.getEndTime());
            m.put("duration", e.getDuration()); m.put("status", e.getStatus());
            m.put("myStatus", s != null ? s.getStatus() : 0);
            m.put("totalScore", s != null ? s.getTotalScore() : null);
            m.put("studentExamId", s != null ? s.getId() : null);
            return m;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Map<String,Object> startExam(Long examId, Long userId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) throw new BusinessException(ErrorCode.EXAM_NOT_FOUND);
        // 自动修正状态：如果考试时间已到但状态还是未开始，自动切换
        if (exam.getStatus() == 0) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(exam.getStartTime()) && now.isBefore(exam.getEndTime())) {
                exam.setStatus(1);
                examMapper.updateById(exam);
            } else {
                throw new BusinessException(ErrorCode.EXAM_NOT_STARTED);
            }
        }
        if (exam.getStatus() >= 2) throw new BusinessException(ErrorCode.EXAM_ENDED);
        // 校验考生范围：指定学员时，不在名单内禁止参加
        if (exam.getStudentScope() == 2 &&
                (exam.getStudentIds() == null || !exam.getStudentIds().contains(userId.toString()))) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "您不在本场考试的考生范围内");
        }

        ExamStudent s = getOrCreateStudentRecord(examId, userId);
        if (s.getStatus() >= 2) throw new BusinessException(ErrorCode.EXAM_ALREADY_SUBMITTED);

        s.setStatus(1);
        s.setStartTime(LocalDateTime.now());
        examStudentMapper.updateById(s);

        // 返回试卷题目
        List<PaperQuestion> pqs = paperQuestionMapper.selectList(
                Wrappers.lambdaQuery(PaperQuestion.class).eq(PaperQuestion::getPaperId, exam.getPaperId()).orderByAsc(PaperQuestion::getSortOrder));

        List<Map<String,Object>> questions = pqs.stream().map(pq -> {
            Map<String,Object> q = new HashMap<>();
            q.put("paperQuestionId", pq.getId());
            q.put("questionType", pq.getQuestionType());
            q.put("title", pq.getTitleSnapshot());
            q.put("options", pq.getOptionsSnapshot() != null ? JSONUtil.parseArray(pq.getOptionsSnapshot()) : Collections.emptyList());
            q.put("score", pq.getScore());
            q.put("sortOrder", pq.getSortOrder());
            return q;
        }).collect(Collectors.toList());

        // 选项乱序
        if (exam.getStatus() != null) {
            ExamPaper paper = paperMapper.selectById(exam.getPaperId());
            if (paper != null && paper.getShuffleOption() == 1) {
                questions.forEach(q -> {
                    List<Map<String,Object>> opts = (List<Map<String,Object>>) q.get("options");
                    if (opts != null) Collections.shuffle(opts);
                });
            }
        }

        Map<String,Object> result = new HashMap<>();
        result.put("examStudentId", s.getId());
        result.put("duration", exam.getDuration());
        result.put("startTime", s.getStartTime());
        result.put("questions", questions);
        return result;
    }

    @Transactional
    public void saveAnswer(Long examStudentId, Long userId, Long paperQuestionId, String answer) {
        ExamStudent s = examStudentMapper.selectById(examStudentId);
        if (s == null || !s.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        if (s.getStatus() != 1) throw new BusinessException(ErrorCode.EXAM_ALREADY_SUBMITTED);

        AnswerRecord ar = answerRecordMapper.selectOne(Wrappers.lambdaQuery(AnswerRecord.class)
                .eq(AnswerRecord::getExamStudentId, examStudentId)
                .eq(AnswerRecord::getPaperQuestionId, paperQuestionId));
        // 将答案转为 JSON 数组字符串存入 MySQL JSON 列
        // 前端发送: JSON.stringify(["A"]) → '["A"]' (已 JSON)
        // API 测试: 直接发送 "A" 或 "A,B" (纯字符串)
        String jsonAnswer;
        if (answer == null || answer.isEmpty()) {
            jsonAnswer = "[]";
        } else if (answer.trim().startsWith("[")) {
            // 已经是 JSON 数组格式（来自前端），直接使用
            jsonAnswer = answer;
        } else if (answer.contains(",")) {
            // 逗号分隔的多选答案: "A,B" → ["A","B"]
            jsonAnswer = JSONUtil.toJsonStr(
                    java.util.Arrays.stream(answer.split(","))
                            .map(String::trim)
                            .collect(java.util.stream.Collectors.toList()));
        } else {
            // 普通字符串，包装为 JSON 数组: "A" → ["A"]
            jsonAnswer = JSONUtil.toJsonStr(java.util.Collections.singletonList(answer));
        }
        if (ar == null) {
            ar = new AnswerRecord();
            ar.setExamStudentId(examStudentId);
            ar.setExamId(s.getExamId());
            ar.setPaperQuestionId(paperQuestionId);
            ar.setUserId(userId);
            ar.setUserAnswer(jsonAnswer);
            answerRecordMapper.insert(ar);
        } else {
            ar.setUserAnswer(jsonAnswer);
            answerRecordMapper.updateById(ar);
        }
    }

    @Transactional
    public Map<String,Object> submitExam(Long examStudentId, Long userId) {
        ExamStudent s = examStudentMapper.selectById(examStudentId);
        if (s == null || !s.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        if (s.getStatus() != 1) throw new BusinessException(ErrorCode.EXAM_ALREADY_SUBMITTED);

        // CAS: 原子地将状态从 1(进行中) 更新为 2(已交卷)，防止并发重复提交
        ExamStudent update = new ExamStudent();
        update.setId(examStudentId);
        update.setStatus(2); // 预占位
        int rows = examStudentMapper.update(update,
                Wrappers.lambdaQuery(ExamStudent.class)
                        .eq(ExamStudent::getId, examStudentId)
                        .eq(ExamStudent::getStatus, 1)); // 仅当状态仍为 1 时更新
        if (rows == 0) {
            throw new BusinessException(ErrorCode.EXAM_ALREADY_SUBMITTED);
        }

        return gradeAndSubmit(s);
    }

    private Map<String,Object> gradeAndSubmit(ExamStudent s) {
        s.setSubmitTime(LocalDateTime.now());
        s.setUsedTime((int) ChronoUnit.SECONDS.between(s.getStartTime(), s.getSubmitTime()));
        s.setStatus(2);

        // 自动判分
        List<AnswerRecord> answers = answerRecordMapper.selectList(
                Wrappers.lambdaQuery(AnswerRecord.class).eq(AnswerRecord::getExamStudentId, s.getId()));
        List<PaperQuestion> pqs = paperQuestionMapper.selectList(
                Wrappers.lambdaQuery(PaperQuestion.class).eq(PaperQuestion::getPaperId,
                        examMapper.selectById(s.getExamId()).getPaperId()));

        int objScore = 0;
        boolean hasSubjective = false;
        for (AnswerRecord ar : answers) {
            PaperQuestion pq = pqs.stream().filter(p -> p.getId().equals(ar.getPaperQuestionId())).findFirst().orElse(null);
            if (pq == null) continue;

            if (pq.getQuestionType() != 4) { // 客观题
                List<String> correct = JSONUtil.toList(pq.getAnswerSnapshot(), String.class);
                List<String> userAns = JSONUtil.toList(ar.getUserAnswer(), String.class);
                boolean correct_ = correct != null && userAns != null && correct.size() == userAns.size() && new HashSet<>(correct).containsAll(userAns);
                ar.setIsCorrect(correct_ ? 1 : 0);
                int sc = correct_ ? pq.getScore() : 0;
                ar.setScore(sc);
                objScore += sc;
                answerRecordMapper.updateById(ar);
            } else {
                hasSubjective = true;
            }
        }

        s.setObjectiveScore(objScore);
        s.setTotalScore(objScore); // 主观题得分后续阅卷后更新
        Exam exam = examMapper.selectById(s.getExamId());
        s.setGradingStatus(hasSubjective ? 0 : 1);
        if (!hasSubjective) {
            s.setStatus(3); // 已出分
            s.setIsPassed(s.getTotalScore() >= (exam.getPassScore() != null ? exam.getPassScore() : 60) ? 1 : 0);
        }
        examStudentMapper.updateById(s);

        // 错题加入错题本
        for (AnswerRecord ar : answers) {
            if (ar.getIsCorrect() != null && ar.getIsCorrect() == 0) {
                PaperQuestion pq = pqs.stream().filter(p -> p.getId().equals(ar.getPaperQuestionId())).findFirst().orElse(null);
                if (pq != null) {
                    // wrongQuestionService.addWrongQuestion(s.getUserId(), pq.getQuestionId(), s.getExamId(), s.getId());
                }
            }
        }

        Map<String,Object> res = new HashMap<>();
        res.put("totalScore", s.getTotalScore());
        res.put("objectiveScore", s.getObjectiveScore());
        res.put("subjectiveScore", s.getSubjectiveScore());
        res.put("isPassed", s.getIsPassed());
        res.put("hasSubjective", hasSubjective);
        return res;
    }

    public Map<String,Object> getResult(Long examStudentId, Long userId) {
        ExamStudent s = examStudentMapper.selectById(examStudentId);
        if (s == null || !s.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        if (s.getStatus() < 2) throw new BusinessException(ErrorCode.FORBIDDEN, "考试未交卷，无法查看成绩");
        List<AnswerRecord> answers = answerRecordMapper.selectList(
                Wrappers.lambdaQuery(AnswerRecord.class).eq(AnswerRecord::getExamStudentId, examStudentId));
        Map<String,Object> res = new HashMap<>();
        res.put("examStudent", s);
        res.put("answers", answers);
        return res;
    }

    // ============ 内部 ============
    private ExamStudent getOrCreateStudentRecord(Long examId, Long userId) {
        ExamStudent s = examStudentMapper.selectOne(Wrappers.lambdaQuery(ExamStudent.class)
                .eq(ExamStudent::getExamId, examId).eq(ExamStudent::getUserId, userId));
        if (s == null) {
            s = new ExamStudent();
            s.setExamId(examId); s.setUserId(userId); s.setStatus(0);
            s.setObjectiveScore(0); s.setSubjectiveScore(0); s.setTotalScore(0);
            s.setScreenSwitches(0); s.setIsCheated(0);
            try {
                examStudentMapper.insert(s);
            } catch (Exception e) {
                // 并发创建时可能重复，重新查询
                s = examStudentMapper.selectOne(Wrappers.lambdaQuery(ExamStudent.class)
                        .eq(ExamStudent::getExamId, examId).eq(ExamStudent::getUserId, userId));
                if (s == null) throw e;
            }
        }
        return s;
    }

    private ExamStudent getStudentRecord(Long examId, Long userId) {
        return examStudentMapper.selectOne(Wrappers.lambdaQuery(ExamStudent.class)
                .eq(ExamStudent::getExamId, examId).eq(ExamStudent::getUserId, userId));
    }
}
