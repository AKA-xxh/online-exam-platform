package com.exam.module.course.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.exam.common.result.PageResult;
import com.exam.common.result.Result;
import com.exam.module.course.dto.*;
import com.exam.module.course.entity.*;
import com.exam.module.course.service.CourseService;
import com.exam.module.course.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "课程管理（管理员）", description = "课程分类、课程、章节、课时的 CRUD")
@RestController
@RequestMapping("/api/v1/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final CourseService courseService;

    // ============ 分类管理 ============
    @Operation(summary = "获取课程分类树")
    @GetMapping("/categories")
    public Result<List<CategoryTreeVO>> getCategories() {
        return Result.ok(courseService.getCategoryTree());
    }

    @Operation(summary = "新增分类")
    @PostMapping("/categories")
    public Result<CourseCategory> createCategory(@RequestBody CourseCategory category) {
        return Result.ok(courseService.createCategory(category));
    }

    @Operation(summary = "更新分类")
    @PutMapping("/categories/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody CourseCategory category) {
        category.setId(id);
        courseService.updateCategory(category);
        return Result.ok();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        courseService.deleteCategory(id);
        return Result.ok();
    }

    // ============ 课程管理 ============
    @Operation(summary = "课程列表")
    @GetMapping
    public Result<PageResult<CourseVO>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(courseService.getCoursePage(categoryId, keyword, status, page, pageSize));
    }

    @Operation(summary = "创建课程")
    @PostMapping
    public Result<Course> create(@Valid @RequestBody CourseCreateDTO dto) {
        return Result.ok(courseService.createCourse(dto, StpUtil.getLoginIdAsLong()));
    }

    @Operation(summary = "更新课程")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CourseUpdateDTO dto) {
        dto.setId(id);
        courseService.updateCourse(dto);
        return Result.ok();
    }

    @Operation(summary = "发布/下架课程")
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        courseService.updateStatus(id, status);
        return Result.ok();
    }

    @Operation(summary = "删除课程")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return Result.ok();
    }

    // ============ 章节课时管理 ============
    @Operation(summary = "新增章节")
    @PostMapping("/{courseId}/chapters")
    public Result<Chapter> createChapter(@PathVariable Long courseId, @Valid @RequestBody ChapterDTO dto) {
        return Result.ok(courseService.createChapter(courseId, dto));
    }

    @Operation(summary = "更新章节")
    @PutMapping("/chapters/{id}")
    public Result<Void> updateChapter(@PathVariable Long id, @RequestBody ChapterDTO dto) {
        courseService.updateChapter(id, dto);
        return Result.ok();
    }

    @Operation(summary = "删除章节")
    @DeleteMapping("/chapters/{id}")
    public Result<Void> deleteChapter(@PathVariable Long id) {
        courseService.deleteChapter(id);
        return Result.ok();
    }

    @Operation(summary = "章节排序")
    @PostMapping("/chapters/sort")
    public Result<Void> sortChapters(@RequestBody List<Map<String, Object>> items) {
        courseService.sortChapters(items);
        return Result.ok();
    }

    @Operation(summary = "新增课时")
    @PostMapping("/chapters/{chapterId}/lessons")
    public Result<Lesson> createLesson(@PathVariable Long chapterId, @RequestParam Long courseId, @Valid @RequestBody LessonDTO dto) {
        return Result.ok(courseService.createLesson(chapterId, courseId, dto));
    }

    @Operation(summary = "更新课时")
    @PutMapping("/lessons/{id}")
    public Result<Void> updateLesson(@PathVariable Long id, @RequestBody LessonDTO dto) {
        courseService.updateLesson(id, dto);
        return Result.ok();
    }

    @Operation(summary = "删除课时")
    @DeleteMapping("/lessons/{id}")
    public Result<Void> deleteLesson(@PathVariable Long id) {
        courseService.deleteLesson(id);
        return Result.ok();
    }
}
