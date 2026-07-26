package com.exam.module.question.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("question_option")
public class QuestionOption {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionId;
    private String optionLabel;  // A/B/C/D...
    private String optionText;
    private Integer isCorrect;   // 0否 1是
    private Integer sortOrder;
}
