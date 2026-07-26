package com.exam.module.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("course_lesson")
public class Lesson {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long chapterId;
    private Long courseId;
    private String title;
    private Integer lessonType;  // 1视频 2图文 3直播
    private String videoUrl;
    private Integer videoDuration;
    private String content;
    private String attachments;  // JSON
    private Integer isFree;
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
