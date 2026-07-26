package com.exam.module.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseCreateDTO {
    @NotNull private Long categoryId;
    @NotBlank private String title;
    private String subtitle;
    private String coverUrl;
    private String description;
    private String teacherName;
    private String teacherIntro;
}
