package com.exam.module.course.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterVO {
    private Long id;
    private String title;
    private Integer sortOrder;
    private List<LessonVO> lessons;
}
