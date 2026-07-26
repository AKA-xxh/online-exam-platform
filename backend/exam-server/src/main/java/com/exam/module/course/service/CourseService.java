package com.exam.module.course.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.exception.BusinessException;
import com.exam.common.result.ErrorCode;
import com.exam.common.result.PageResult;
import com.exam.module.course.dto.*;
import com.exam.module.course.entity.*;
import com.exam.module.course.mapper.*;
import com.exam.module.course.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseMapper courseMapper;
    private final CourseCategoryMapper categoryMapper;
    private final ChapterMapper chapterMapper;
    private final LessonMapper lessonMapper;
    private final LearnRecordMapper learnRecordMapper;

    // ============ 分类管理 ============

    public List<CategoryTreeVO> getCategoryTree() {
        List<CourseCategory> all = categoryMapper.selectList(Wrappers.lambdaQuery(CourseCategory.class)
                .eq(CourseCategory::getStatus, 1).orderByAsc(CourseCategory::getSortOrder));
        return buildCategoryTree(all, 0L);
    }

    private List<CategoryTreeVO> buildCategoryTree(List<CourseCategory> list, Long parentId) {
        return list.stream().filter(c -> c.getParentId().equals(parentId))
                .map(c -> CategoryTreeVO.builder()
                        .id(c.getId()).name(c.getName()).icon(c.getIcon())
                        .sortOrder(c.getSortOrder())
                        .children(buildCategoryTree(list, c.getId())).build())
                .collect(Collectors.toList());
    }

    public CourseCategory createCategory(CourseCategory category) {
        categoryMapper.insert(category);
        return category;
    }

    public void updateCategory(CourseCategory category) {
        categoryMapper.updateById(category);
    }

    public void deleteCategory(Long id) {
        Long count = courseMapper.selectCount(Wrappers.lambdaQuery(Course.class).eq(Course::getCategoryId, id));
        if (count > 0) throw new BusinessException(ErrorCode.PARAM_INVALID, "分类下有课程，无法删除");
        categoryMapper.deleteById(id);
    }

    // ============ 课程 CRUD ============

    public PageResult<CourseVO> getCoursePage(Long categoryId, String keyword, Integer status, int page, int pageSize) {
        LambdaQueryWrapper<Course> qw = Wrappers.lambdaQuery(Course.class)
                .eq(status != null, Course::getStatus, status)
                .eq(categoryId != null, Course::getCategoryId, categoryId)
                .like(keyword != null, Course::getTitle, keyword)
                .orderByDesc(Course::getCreateTime);
        Page<Course> mpPage = new Page<>(page, pageSize);
        courseMapper.selectPage(mpPage, qw);
        List<CourseVO> records = mpPage.getRecords().stream().map(c -> {
            CourseCategory cat = categoryMapper.selectById(c.getCategoryId());
            return CourseVO.builder().id(c.getId()).categoryId(c.getCategoryId())
                    .categoryName(cat != null ? cat.getName() : "")
                    .title(c.getTitle()).subtitle(c.getSubtitle()).coverUrl(c.getCoverUrl())
                    .description(c.getDescription()).teacherName(c.getTeacherName())
                    .teacherIntro(c.getTeacherIntro()).totalDuration(c.getTotalDuration())
                    .lessonCount(c.getLessonCount()).studentCount(c.getStudentCount())
                    .rating(c.getRating()).status(c.getStatus()).createTime(c.getCreateTime()).build();
        }).collect(Collectors.toList());
        return new PageResult<>(records, mpPage.getTotal(), page, pageSize, mpPage.getPages());
    }

    public CourseDetailVO getCourseDetail(Long courseId, Long userId) {
        Course c = courseMapper.selectById(courseId);
        if (c == null) throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        CourseCategory cat = categoryMapper.selectById(c.getCategoryId());
        CourseVO courseVO = CourseVO.builder().id(c.getId()).categoryId(c.getCategoryId())
                .categoryName(cat != null ? cat.getName() : "").title(c.getTitle())
                .subtitle(c.getSubtitle()).coverUrl(c.getCoverUrl()).description(c.getDescription())
                .teacherName(c.getTeacherName()).teacherIntro(c.getTeacherIntro())
                .totalDuration(c.getTotalDuration()).lessonCount(c.getLessonCount())
                .studentCount(c.getStudentCount()).rating(c.getRating()).status(c.getStatus())
                .createTime(c.getCreateTime()).build();

        List<Chapter> chapters = chapterMapper.selectList(
                Wrappers.lambdaQuery(Chapter.class).eq(Chapter::getCourseId, courseId).orderByAsc(Chapter::getSortOrder));
        List<ChapterVO> chapterVOs = chapters.stream().map(ch -> {
            List<Lesson> lessons = lessonMapper.selectList(
                    Wrappers.lambdaQuery(Lesson.class).eq(Lesson::getChapterId, ch.getId()).orderByAsc(Lesson::getSortOrder));
            List<LessonVO> lessonVOs = lessons.stream().map(ls -> {
                LearnRecord lr = null;
                if (userId != null) {
                    lr = learnRecordMapper.selectOne(Wrappers.lambdaQuery(LearnRecord.class)
                            .eq(LearnRecord::getUserId, userId).eq(LearnRecord::getLessonId, ls.getId()));
                }
                return LessonVO.builder().id(ls.getId()).title(ls.getTitle()).lessonType(ls.getLessonType())
                        .videoUrl(ls.getVideoUrl()).videoDuration(ls.getVideoDuration())
                        .content(ls.getContent()).attachments(ls.getAttachments())
                        .isFree(ls.getIsFree()).sortOrder(ls.getSortOrder())
                        .progress(lr != null ? lr.getProgress() : 0)
                        .isFinished(lr != null ? lr.getIsFinished() : 0).build();
            }).collect(Collectors.toList());
            return ChapterVO.builder().id(ch.getId()).title(ch.getTitle()).sortOrder(ch.getSortOrder()).lessons(lessonVOs).build();
        }).collect(Collectors.toList());

        return CourseDetailVO.builder().course(courseVO).chapters(chapterVOs).build();
    }

    @Transactional
    public Course createCourse(CourseCreateDTO dto, Long userId) {
        Course course = new Course();
        BeanUtil.copyProperties(dto, course);
        course.setCreateBy(userId);
        course.setStatus(0);
        courseMapper.insert(course);
        return course;
    }

    @Transactional
    public void updateCourse(CourseUpdateDTO dto) {
        Course course = courseMapper.selectById(dto.getId());
        if (course == null) throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        BeanUtil.copyProperties(dto, course, "id", "createBy", "createTime");
        courseMapper.updateById(course);
    }

    public void updateStatus(Long id, Integer status) {
        Course course = courseMapper.selectById(id);
        if (course == null) throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        course.setStatus(status);
        courseMapper.updateById(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        courseMapper.deleteById(id);
        chapterMapper.delete(Wrappers.lambdaQuery(Chapter.class).eq(Chapter::getCourseId, id));
        lessonMapper.delete(Wrappers.lambdaQuery(Lesson.class).eq(Lesson::getCourseId, id));
    }

    // ============ 章节课时管理 ============

    @Transactional
    public Chapter createChapter(Long courseId, ChapterDTO dto) {
        Chapter ch = new Chapter();
        ch.setCourseId(courseId);
        ch.setTitle(dto.getTitle());
        ch.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        chapterMapper.insert(ch);
        return ch;
    }

    @Transactional
    public Lesson createLesson(Long chapterId, Long courseId, LessonDTO dto) {
        Lesson ls = new Lesson();
        BeanUtil.copyProperties(dto, ls);
        ls.setChapterId(chapterId);
        ls.setCourseId(courseId);
        lessonMapper.insert(ls);
        // 更新课程课时数
        updateCourseLessonCount(courseId);
        return ls;
    }

    @Transactional
    public void updateChapter(Long id, ChapterDTO dto) {
        Chapter ch = chapterMapper.selectById(id);
        if (ch == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        BeanUtil.copyProperties(dto, ch, "id", "courseId");
        chapterMapper.updateById(ch);
    }

    @Transactional
    public void updateLesson(Long id, LessonDTO dto) {
        Lesson ls = lessonMapper.selectById(id);
        if (ls == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        BeanUtil.copyProperties(dto, ls, "id", "chapterId", "courseId");
        lessonMapper.updateById(ls);
    }

    public void deleteChapter(Long id) {
        lessonMapper.delete(Wrappers.lambdaQuery(Lesson.class).eq(Lesson::getChapterId, id));
        chapterMapper.deleteById(id);
    }

    public void deleteLesson(Long id) {
        Lesson ls = lessonMapper.selectById(id);
        if (ls != null) {
            lessonMapper.deleteById(id);
            updateCourseLessonCount(ls.getCourseId());
        }
    }

    @Transactional
    public void sortChapters(List<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            Chapter ch = new Chapter();
            ch.setId(Long.valueOf(item.get("id").toString()));
            ch.setSortOrder(Integer.parseInt(item.get("sortOrder").toString()));
            chapterMapper.updateById(ch);
        }
    }

    private void updateCourseLessonCount(Long courseId) {
        Long count = lessonMapper.selectCount(
                Wrappers.lambdaQuery(Lesson.class).eq(Lesson::getCourseId, courseId));
        Course c = new Course();
        c.setId(courseId);
        c.setLessonCount(count.intValue());
        courseMapper.updateById(c);
    }

    // ============ 学习进度 ============

    public void reportProgress(Long userId, LearnProgressDTO dto) {
        LearnRecord record = learnRecordMapper.selectOne(Wrappers.lambdaQuery(LearnRecord.class)
                .eq(LearnRecord::getUserId, userId).eq(LearnRecord::getLessonId, dto.getLessonId()));
        if (record == null) {
            record = new LearnRecord();
            record.setUserId(userId);
            record.setCourseId(dto.getCourseId());
            record.setLessonId(dto.getLessonId());
            record.setProgress(dto.getProgress());
            record.setDuration(dto.getDuration());
            record.setIsFinished(dto.getProgress() != null && dto.getProgress() >= 90 ? 1 : 0);
            learnRecordMapper.insert(record);
        } else {
            record.setProgress(Math.max(record.getProgress(), dto.getProgress() != null ? dto.getProgress() : 0));
            record.setDuration((record.getDuration() != null ? record.getDuration() : 0) + (dto.getDuration() != null ? dto.getDuration() : 0));
            if (dto.getProgress() != null && dto.getProgress() >= 90) record.setIsFinished(1);
            learnRecordMapper.updateById(record);
        }
    }

    public List<CourseVO> getMyLearningCourses(Long userId) {
        List<LearnRecord> records = learnRecordMapper.selectList(
                Wrappers.lambdaQuery(LearnRecord.class).eq(LearnRecord::getUserId, userId));
        Set<Long> courseIds = records.stream().map(LearnRecord::getCourseId).collect(Collectors.toSet());
        if (courseIds.isEmpty()) return Collections.emptyList();
        List<Course> courses = courseMapper.selectBatchIds(courseIds);
        return courses.stream().map(c -> {
            CourseCategory cat = categoryMapper.selectById(c.getCategoryId());
            long finished = records.stream().filter(r -> r.getCourseId().equals(c.getId()) && r.getIsFinished() == 1).count();
            long total = lessonMapper.selectCount(Wrappers.lambdaQuery(Lesson.class).eq(Lesson::getCourseId, c.getId()));
            return CourseVO.builder().id(c.getId()).categoryId(c.getCategoryId())
                    .categoryName(cat != null ? cat.getName() : "").title(c.getTitle())
                    .coverUrl(c.getCoverUrl()).teacherName(c.getTeacherName())
                    .lessonCount(c.getLessonCount())
                    .progress(total > 0 ? (int) (finished * 100 / total) : 0)
                    .createTime(c.getCreateTime()).build();
        }).collect(Collectors.toList());
    }
}
