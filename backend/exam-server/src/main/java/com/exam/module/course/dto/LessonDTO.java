package com.exam.module.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonDTO {
    private Long id;
    @NotBlank private String title;
    @NotNull private Integer lessonType;
    private String videoUrl;
    private Integer videoDuration;
    private String content;
    private String attachments;
    private Integer isFree;
    private Integer sortOrder;
}
