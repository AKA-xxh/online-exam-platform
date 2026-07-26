package com.exam.module.exam.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.exam.common.result.PageResult;
import com.exam.common.result.Result;
import com.exam.module.exam.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "考试管理（管理员）", description = "发布考试、考试监控、强制收卷")
@RestController
@RequestMapping("/api/v1/admin/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @Operation(summary = "考试列表")
    @GetMapping
    public Result<PageResult<Map<String,Object>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(examService.getPage(keyword, status, page, pageSize));
    }

    @Operation(summary = "发布考试")
    @PostMapping
    public Result<?> publish(@RequestBody Map<String,Object> data) {
        return Result.ok(examService.publish(data, StpUtil.getLoginIdAsLong()));
    }

    @Operation(summary = "取消考试")
    @PatchMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) { examService.cancel(id); return Result.ok(); }

    @Operation(summary = "考试监控")
    @GetMapping("/{id}/monitor")
    public Result<Map<String,Object>> monitor(@PathVariable Long id) { return Result.ok(examService.getMonitor(id)); }

    @Operation(summary = "强制收卷")
    @PostMapping("/{id}/force-submit/{userId}")
    public Result<Void> forceSubmit(@PathVariable Long id, @PathVariable Long userId) { examService.forceSubmit(id, userId); return Result.ok(); }
}
