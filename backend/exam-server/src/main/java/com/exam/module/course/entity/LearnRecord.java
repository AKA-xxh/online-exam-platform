package com.exam.module.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("learn_record")
public class LearnRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long courseId;
    private Long lessonId;
    private Integer progress;
    private Integer duration;
    private Integer isFinished;
    private LocalDateTime finishedTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
