package com.exam.module.course.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String subtitle;
    private String coverUrl;
    private String description;
    private String teacherName;
    private String teacherIntro;
    private Integer totalDuration;
    private Integer lessonCount;
    private Integer studentCount;
    private BigDecimal rating;
    private Integer status;
    private Integer progress;
    private LocalDateTime createTime;
}
