package com.exam.module.course.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LearnProgressDTO {
    @NotNull private Long lessonId;
    @NotNull private Long courseId;
    private Integer progress;
    private Integer duration;
}
