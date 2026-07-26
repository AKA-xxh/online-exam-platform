package com.exam.module.paper.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("paper_question")
public class PaperQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long paperId;
    private Long questionId;
    private String sectionName;
    private Integer questionType;
    private String titleSnapshot;
    private String optionsSnapshot; // JSON
    private String answerSnapshot; // JSON
    private String analysisSnapshot;
    private Integer score;
    private Integer sortOrder;
}
