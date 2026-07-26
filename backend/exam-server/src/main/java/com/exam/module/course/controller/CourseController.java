package com.exam.module.course.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.exam.common.result.PageResult;
import com.exam.common.result.Result;
import com.exam.module.course.dto.*;
import com.exam.module.course.service.CourseService;
import com.exam.module.course.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "课程（学员端）", description = "课程列表、详情、学习进度")
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "课程列表（学员端）")
    @GetMapping
    public Result<PageResult<CourseVO>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(courseService.getCoursePage(categoryId, keyword, 1, page, pageSize));
    }

    @Operation(summary = "课程详情")
    @GetMapping("/{id}")
    public Result<CourseDetailVO> detail(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(courseService.getCourseDetail(id, userId));
    }

    @Operation(summary = "课程分类树")
    @GetMapping("/categories")
    public Result<List<CategoryTreeVO>> categories() {
        return Result.ok(courseService.getCategoryTree());
    }

    @Operation(summary = "我的学习课程")
    @GetMapping("/my")
    public Result<List<CourseVO>> myCourses() {
        return Result.ok(courseService.getMyLearningCourses(StpUtil.getLoginIdAsLong()));
    }

    @Operation(summary = "上报学习进度")
    @PostMapping("/progress")
    public Result<Void> progress(@RequestBody LearnProgressDTO dto) {
        courseService.reportProgress(StpUtil.getLoginIdAsLong(), dto);
        return Result.ok();
    }
}
