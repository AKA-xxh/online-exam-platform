package com.exam.module.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
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
    private Integer status;  // 0草稿 1已发布 2已下架
    @TableLogic
    private Integer isDeleted;
    private Long createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
