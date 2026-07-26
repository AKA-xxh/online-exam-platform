package com.exam.module.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("answer_record")
public class AnswerRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examStudentId;
    private Long examId;
    private Long paperQuestionId;
    private Long userId;
    private String userAnswer;   // JSON
    private Integer isCorrect;   // 0错 1对 null主观题
    private Integer score;
    private Long graderId;
    private String graderComment;
    private LocalDateTime gradedTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
