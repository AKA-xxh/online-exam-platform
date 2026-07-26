package com.exam.module.course.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChapterDTO {
    private Long id;
    @NotBlank private String title;
    private Integer sortOrder;
    private java.util.List<LessonDTO> lessons;
}
