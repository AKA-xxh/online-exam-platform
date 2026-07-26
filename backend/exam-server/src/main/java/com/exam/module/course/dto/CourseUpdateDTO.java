package com.exam.module.course.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CourseUpdateDTO extends CourseCreateDTO {
    @NotNull private Long id;
}
