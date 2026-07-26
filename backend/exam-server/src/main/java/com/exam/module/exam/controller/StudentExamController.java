package com.exam.module.exam.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.exam.common.result.Result;
import com.exam.module.exam.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "考试（学员端）", description = "考试列表、开始考试、答题、交卷、成绩")
@RestController
@RequestMapping("/api/v1/student/exams")
@RequiredArgsConstructor
public class StudentExamController {

    private final ExamService examService;

    @Operation(summary = "我的考试列表")
    @GetMapping
    public Result<List<Map<String,Object>>> myExams() { return Result.ok(examService.getMyExams(StpUtil.getLoginIdAsLong())); }

    @Operation(summary = "开始考试（获取试卷）")
    @PostMapping("/{examId}/start")
    public Result<Map<String,Object>> start(@PathVariable Long examId) {
        return Result.ok(examService.startExam(examId, StpUtil.getLoginIdAsLong()));
    }

    @Operation(summary = "保存答案")
    @PostMapping("/answers")
    public Result<Void> saveAnswer(@RequestBody Map<String,Object> data) {
        examService.saveAnswer(
                Long.valueOf(data.get("examStudentId").toString()),
                StpUtil.getLoginIdAsLong(),
                Long.valueOf(data.get("paperQuestionId").toString()),
                data.get("answer") != null ? data.get("answer").toString() : null);
        return Result.ok();
    }

    @Operation(summary = "交卷")
    @PostMapping("/{examStudentId}/submit")
    public Result<Map<String,Object>> submit(@PathVariable Long examStudentId) {
        return Result.ok(examService.submitExam(examStudentId, StpUtil.getLoginIdAsLong()));
    }

    @Operation(summary = "查看成绩")
    @GetMapping("/{examStudentId}/result")
    public Result<Map<String,Object>> result(@PathVariable Long examStudentId) {
        return Result.ok(examService.getResult(examStudentId, StpUtil.getLoginIdAsLong()));
    }
}
