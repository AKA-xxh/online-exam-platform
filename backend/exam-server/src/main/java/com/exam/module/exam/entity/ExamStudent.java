package com.exam.module.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exam_student")
public class ExamStudent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Long userId;
    private Integer status;  // 0未开始 1考试中 2已交卷 3已出分 4缺考
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private Integer usedTime;
    private Integer totalScore;
    private Integer objectiveScore;
    private Integer subjectiveScore;
    private Integer isPassed;
    private Integer gradingStatus;  // 0未阅 1已阅
    private Integer screenSwitches;
    private Integer isCheated;
    private String cheatReason;
    @TableLogic
    private Integer isDeleted;
}
