package com.exam.module.score.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.result.Result;
import com.exam.module.exam.entity.WrongQuestion;
import com.exam.module.exam.mapper.WrongQuestionMapper;
import com.exam.module.question.entity.Question;
import com.exam.module.question.entity.QuestionOption;
import com.exam.module.question.mapper.QuestionMapper;
import com.exam.module.question.mapper.QuestionOptionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "错题本", description = "错题列表、移除、统计")
@RestController
@RequestMapping("/api/v1/student/wrong-questions")
@RequiredArgsConstructor
public class WrongQuestionController {

    private final WrongQuestionMapper wrongQuestionMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;

    @Operation(summary = "我的错题列表")
    @GetMapping
    public Result<Map<String,Object>> list(@RequestParam(required = false) Integer questionType,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        var qw = Wrappers.lambdaQuery(WrongQuestion.class)
                .eq(WrongQuestion::getUserId, userId)
                .eq(WrongQuestion::getIsRemoved, 0)
                .orderByDesc(WrongQuestion::getCreateTime);
        Page<WrongQuestion> mp = wrongQuestionMapper.selectPage(new Page<>(page, pageSize), qw);

        List<Map<String,Object>> records = mp.getRecords().stream().map(wq -> {
            Question q = questionMapper.selectById(wq.getQuestionId());
            Map<String,Object> m = new HashMap<>();
            m.put("id", wq.getId());
            m.put("questionId", wq.getQuestionId());
            m.put("examId", wq.getExamId());
            m.put("wrongCount", wq.getWrongCount());
            m.put("title", q != null ? q.getTitle() : "");
            m.put("questionType", q != null ? q.getQuestionType() : 0);
            m.put("difficulty", q != null ? q.getDifficulty() : 1);
            m.put("createTime", wq.getCreateTime());
            return m;
        }).collect(Collectors.toList());

        if (questionType != null) {
            records = records.stream().filter(r -> Integer.valueOf(r.get("questionType").toString()).equals(questionType)).collect(Collectors.toList());
        }

        Map<String,Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", mp.getTotal());
        return Result.ok(result);
    }

    @Operation(summary = "错题详情（含选项和答案）")
    @GetMapping("/{id}")
    public Result<Map<String,Object>> detail(@PathVariable Long id) {
        WrongQuestion wq = wrongQuestionMapper.selectById(id);
        if (wq == null) return Result.fail(404, "记录不存在");
        Question q = questionMapper.selectById(wq.getQuestionId());
        List<QuestionOption> options = questionOptionMapper.selectList(
                Wrappers.lambdaQuery(QuestionOption.class).eq(QuestionOption::getQuestionId, wq.getQuestionId()).orderByAsc(QuestionOption::getSortOrder));
        Map<String,Object> m = new HashMap<>();
        m.put("wrongQuestion", wq);
        m.put("question", q);
        m.put("options", options);
        return Result.ok(m);
    }

    @Operation(summary = "移除错题（标记已掌握）")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        WrongQuestion wq = wrongQuestionMapper.selectById(id);
        if (wq != null) { wq.setIsRemoved(1); wrongQuestionMapper.updateById(wq); }
        return Result.ok();
    }

    @Operation(summary = "错题统计")
    @GetMapping("/stats")
    public Result<Map<String,Object>> stats() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<WrongQuestion> all = wrongQuestionMapper.selectList(
                Wrappers.lambdaQuery(WrongQuestion.class).eq(WrongQuestion::getUserId, userId).eq(WrongQuestion::getIsRemoved, 0));
        Map<String,Object> stats = new HashMap<>();
        stats.put("total", all.size());
        stats.put("singleChoice", all.stream().filter(wq -> {
            Question q = questionMapper.selectById(wq.getQuestionId());
            return q != null && q.getQuestionType() == 1;
        }).count());
        stats.put("multiChoice", all.stream().filter(wq -> {
            Question q = questionMapper.selectById(wq.getQuestionId());
            return q != null && q.getQuestionType() == 2;
        }).count());
        stats.put("trueFalse", all.stream().filter(wq -> {
            Question q = questionMapper.selectById(wq.getQuestionId());
            return q != null && q.getQuestionType() == 3;
        }).count());
        stats.put("essay", all.stream().filter(wq -> {
            Question q = questionMapper.selectById(wq.getQuestionId());
            return q != null && q.getQuestionType() == 4;
        }).count());
        return Result.ok(stats);
    }
}
