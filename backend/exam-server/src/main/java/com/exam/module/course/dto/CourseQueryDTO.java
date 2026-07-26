package com.exam.module.course.dto;

import lombok.Data;

@Data
public class CourseQueryDTO {
    private Long categoryId;
    private String keyword;
    private Integer status;
    private Integer page = 1;
    private Integer pageSize = 20;
    private String sortBy;
}
