package com.exam.module.question.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private Integer questionType;  // 1单选 2多选 3判断 4简答
    private String title;
    private String analysis;
    private Integer difficulty;    // 1简单 2中等 3困难
    private String tags;
    private Integer version;
    private Integer useCount;
    private BigDecimal correctRate;
    private Integer status;
    @TableLogic
    private Integer isDeleted;
    private Long createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
