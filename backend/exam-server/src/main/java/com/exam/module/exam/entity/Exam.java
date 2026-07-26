package com.exam.module.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exam")
public class Exam {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long paperId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Integer studentScope;  // 1全部 2指定学员
    private String studentIds;     // JSON
    private Integer status;        // 0未开始 1进行中 2已结束 3已取消
    private Integer passScore;
    @TableLogic
    private Integer isDeleted;
    private Long createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
