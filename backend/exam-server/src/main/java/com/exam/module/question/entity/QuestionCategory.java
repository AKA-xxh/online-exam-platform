package com.exam.module.question.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("question_category")
public class QuestionCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String name;
    private Integer sortOrder;
    @TableField(exist = false)
    private List<QuestionCategory> children;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
