package com.exam.module.paper.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exam_paper")
public class ExamPaper {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private Integer totalScore;
    private Integer passScore;
    private Integer duration;       // 分钟
    private Integer paperType;      // 1手动 2随机
    private String genRules;        // JSON
    private Integer showAnswer;     // 0不展示 1交卷后 2结束后
    private Integer shuffleQuestion;
    private Integer shuffleOption;
    private Integer maxScreenSwitches;
    private Integer enableFaceCheck;
    private String antiCheat;       // JSON
    private Integer status;
    @TableLogic
    private Integer isDeleted;
    private Long createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
