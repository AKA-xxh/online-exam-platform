package com.exam.module.course.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonVO {
    private Long id;
    private String title;
    private Integer lessonType;
    private String videoUrl;
    private Integer videoDuration;
    private String content;
    private String attachments;
    private Integer isFree;
    private Integer sortOrder;
    private Integer progress;
    private Integer isFinished;
}
