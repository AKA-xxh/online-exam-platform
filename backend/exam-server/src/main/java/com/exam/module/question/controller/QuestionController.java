package com.exam.module.question.controller;

import com.exam.common.result.PageResult;
import com.exam.common.result.Result;
import com.exam.module.question.entity.QuestionCategory;
import com.exam.module.question.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "题库管理", description = "题目 CRUD、分类管理、批量操作、统计")
@RestController
@RequestMapping("/api/v1/admin/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    // ============ 分类 ============
    @Operation(summary = "分类树")
    @GetMapping("/categories")
    public Result<List<QuestionCategory>> getCategories() { return Result.ok(questionService.getCategoryTree()); }

    @Operation(summary = "新增分类")
    @PostMapping("/categories")
    public Result<QuestionCategory> createCategory(@RequestBody QuestionCategory c) { return Result.ok(questionService.createCategory(c)); }

    @Operation(summary = "更新分类")
    @PutMapping("/categories/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody QuestionCategory c) { c.setId(id); questionService.updateCategory(c); return Result.ok(); }

    @Operation(summary = "删除分类")
    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) { questionService.deleteCategory(id); return Result.ok(); }

    // ============ 题目 ============
    @Operation(summary = "题目列表")
    @GetMapping
    public Result<PageResult<Map<String,Object>>> list(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(questionService.getPage(type, categoryId, difficulty, status, keyword, page, pageSize));
    }

    @Operation(summary = "题目详情")
    @GetMapping("/{id}")
    public Result<Map<String,Object>> detail(@PathVariable Long id) { return Result.ok(questionService.getDetail(id)); }

    @Operation(summary = "新增题目")
    @PostMapping
    public Result<?> create(@RequestBody Map<String,Object> data) { return Result.ok(questionService.create(data)); }

    @Operation(summary = "编辑题目")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String,Object> data) { questionService.update(id, data); return Result.ok(); }

    @Operation(summary = "删除题目")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { questionService.delete(id); return Result.ok(); }

    @Operation(summary = "批量删除")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) { questionService.batchDelete(ids); return Result.ok(); }

    @Operation(summary = "批量修改状态")
    @PatchMapping("/batch/status")
    public Result<Void> batchUpdateStatus(@RequestBody Map<String,Object> data) {
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) data.get("ids");
        List<Long> ids = rawIds.stream().map(Long::valueOf).collect(java.util.stream.Collectors.toList());
        questionService.batchUpdateStatus(ids, Integer.valueOf(data.get("status").toString())); return Result.ok();
    }

    @Operation(summary = "批量移动分类")
    @PatchMapping("/batch/move")
    public Result<Void> batchMoveCategory(@RequestBody Map<String,Object> data) {
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) data.get("ids");
        List<Long> ids = rawIds.stream().map(Long::valueOf).collect(java.util.stream.Collectors.toList());
        questionService.batchMoveCategory(ids, Long.valueOf(data.get("categoryId").toString())); return Result.ok();
    }

    @Operation(summary = "题库统计")
    @GetMapping("/stats")
    public Result<Map<String,Object>> stats() { return Result.ok(questionService.getStats()); }
}
