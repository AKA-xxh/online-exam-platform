package com.exam.module.paper.controller;

import com.exam.common.result.PageResult;
import com.exam.common.result.Result;
import com.exam.module.paper.service.PaperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "试卷管理", description = "试卷 CRUD、手动组卷、随机组卷、预览")
@RestController
@RequestMapping("/api/v1/admin/papers")
@RequiredArgsConstructor
public class PaperController {

    private final PaperService paperService;

    @Operation(summary = "试卷列表")
    @GetMapping
    public Result<PageResult<Map<String,Object>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer paperType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(paperService.getPage(keyword, paperType, page, pageSize));
    }

    @Operation(summary = "试卷详情")
    @GetMapping("/{id}")
    public Result<Map<String,Object>> detail(@PathVariable Long id) { return Result.ok(paperService.getDetail(id)); }

    @Operation(summary = "创建试卷")
    @PostMapping
    public Result<?> create(@RequestBody Map<String,Object> data) { return Result.ok(paperService.create(data)); }

    @Operation(summary = "更新试卷")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String,Object> data) { paperService.update(id, data); return Result.ok(); }

    @Operation(summary = "删除试卷")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { paperService.delete(id); return Result.ok(); }

    @Operation(summary = "重新随机生成")
    @PostMapping("/{id}/regenerate")
    public Result<Map<String,Object>> regenerate(@PathVariable Long id) { return Result.ok(paperService.regenerate(id)); }
}
