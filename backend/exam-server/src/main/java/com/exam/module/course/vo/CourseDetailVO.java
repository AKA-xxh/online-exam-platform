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
public class CourseDetailVO {
    private CourseVO course;
    private List<ChapterVO> chapters;
}
