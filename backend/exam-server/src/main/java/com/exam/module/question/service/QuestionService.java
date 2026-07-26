package com.exam.module.question.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.exception.BusinessException;
import com.exam.common.result.ErrorCode;
import com.exam.common.result.PageResult;
import com.exam.module.question.entity.*;
import com.exam.module.question.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper optionMapper;
    private final QuestionCategoryMapper categoryMapper;

    // ============ 分类 ============
    public List<QuestionCategory> getCategoryTree() {
        List<QuestionCategory> all = categoryMapper.selectList(Wrappers.lambdaQuery(QuestionCategory.class).orderByAsc(QuestionCategory::getSortOrder));
        return buildTree(all, 0L);
    }

    private List<QuestionCategory> buildTree(List<QuestionCategory> list, Long parentId) {
        return list.stream().filter(c -> c.getParentId().equals(parentId))
                .peek(c -> c.setChildren(buildTree(list, c.getId())))
                .collect(Collectors.toList());
    }

    public QuestionCategory createCategory(QuestionCategory c) { categoryMapper.insert(c); return c; }
    public void updateCategory(QuestionCategory c) { categoryMapper.updateById(c); }

    public void deleteCategory(Long id) {
        Long cnt = questionMapper.selectCount(Wrappers.lambdaQuery(Question.class).eq(Question::getCategoryId, id));
        if (cnt > 0) throw new BusinessException(ErrorCode.PARAM_INVALID, "分类下有题目，无法删除");
        categoryMapper.deleteById(id);
    }

    // ============ 题目 CRUD ============
    public PageResult<Map<String,Object>> getPage(Integer type, Long categoryId, Integer difficulty, Integer status, String keyword, int page, int pageSize) {
        LambdaQueryWrapper<Question> qw = Wrappers.lambdaQuery(Question.class)
                .eq(type != null, Question::getQuestionType, type)
                .eq(categoryId != null, Question::getCategoryId, categoryId)
                .eq(difficulty != null, Question::getDifficulty, difficulty)
                .eq(status != null, Question::getStatus, status)
                .like(keyword != null, Question::getTitle, keyword)
                .orderByDesc(Question::getCreateTime);
        Page<Question> mp = questionMapper.selectPage(new Page<>(page, pageSize), qw);
        List<Map<String,Object>> records = mp.getRecords().stream().map(q -> {
            Map<String,Object> m = new HashMap<>();
            m.put("id", q.getId());
            m.put("categoryId", q.getCategoryId());
            QuestionCategory cat = categoryMapper.selectById(q.getCategoryId());
            m.put("categoryName", cat != null ? cat.getName() : "");
            m.put("questionType", q.getQuestionType());
            m.put("typeName", typeName(q.getQuestionType()));
            m.put("title", q.getTitle().length() > 100 ? q.getTitle().substring(0, 100) + "..." : q.getTitle());
            m.put("difficulty", q.getDifficulty());
            m.put("difficultyName", diffName(q.getDifficulty()));
            m.put("status", q.getStatus());
            m.put("tags", q.getTags());
            m.put("useCount", q.getUseCount());
            m.put("createTime", q.getCreateTime());
            return m;
        }).collect(Collectors.toList());
        return new PageResult<>(records, mp.getTotal(), page, pageSize, mp.getPages());
    }

    public Map<String,Object> getDetail(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null) throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND);
        Map<String,Object> m = new HashMap<>();
        m.put("question", q);
        m.put("options", optionMapper.selectList(Wrappers.lambdaQuery(QuestionOption.class).eq(QuestionOption::getQuestionId, id).orderByAsc(QuestionOption::getSortOrder)));
        return m;
    }

    @Transactional
    public Question create(Map<String,Object> data) {
        Question q = parseQuestion(data);
        q.setCreateBy(0L);
        questionMapper.insert(q);
        saveOptions(q.getId(), data);
        return q;
    }

    @Transactional
    public void update(Long id, Map<String,Object> data) {
        Question q = questionMapper.selectById(id);
        if (q == null) throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND);
        Question updated = parseQuestion(data);
        updated.setId(id);
        questionMapper.updateById(updated);
        optionMapper.delete(Wrappers.lambdaQuery(QuestionOption.class).eq(QuestionOption::getQuestionId, id));
        saveOptions(id, data);
    }

    @Transactional
    public void delete(Long id) { questionMapper.deleteById(id); optionMapper.delete(Wrappers.lambdaQuery(QuestionOption.class).eq(QuestionOption::getQuestionId, id)); }

    @Transactional
    public void batchDelete(List<Long> ids) { ids.forEach(this::delete); }

    @Transactional
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        for (Long id : ids) { Question q = new Question(); q.setId(id); q.setStatus(status); questionMapper.updateById(q); }
    }

    @Transactional
    public void batchMoveCategory(List<Long> ids, Long categoryId) {
        for (Long id : ids) { Question q = new Question(); q.setId(id); q.setCategoryId(categoryId); questionMapper.updateById(q); }
    }

    public Map<String,Object> getStats() {
        Map<String,Object> stats = new HashMap<>();
        stats.put("total", questionMapper.selectCount(null));
        stats.put("singleChoice", questionMapper.selectCount(Wrappers.lambdaQuery(Question.class).eq(Question::getQuestionType, 1)));
        stats.put("multiChoice", questionMapper.selectCount(Wrappers.lambdaQuery(Question.class).eq(Question::getQuestionType, 2)));
        stats.put("trueFalse", questionMapper.selectCount(Wrappers.lambdaQuery(Question.class).eq(Question::getQuestionType, 3)));
        stats.put("essay", questionMapper.selectCount(Wrappers.lambdaQuery(Question.class).eq(Question::getQuestionType, 4)));
        return stats;
    }

    // ============ 内部方法 ============
    private Question parseQuestion(Map<String,Object> data) {
        Question q = new Question();
        q.setCategoryId(Long.valueOf(data.get("categoryId").toString()));
        q.setQuestionType(Integer.valueOf(data.get("questionType").toString()));
        q.setTitle(data.get("title").toString());
        q.setDifficulty(data.get("difficulty") != null ? Integer.valueOf(data.get("difficulty").toString()) : 1);
        q.setAnalysis(data.get("analysis") != null ? data.get("analysis").toString() : null);
        q.setTags(data.get("tags") != null ? data.get("tags").toString() : null);
        q.setVersion(1);
        q.setStatus(1);
        return q;
    }

    @SuppressWarnings("unchecked")
    private void saveOptions(Long questionId, Map<String,Object> data) {
        List<Map<String,Object>> options = (List<Map<String,Object>>) data.get("options");
        if (options == null) return;
        for (int i = 0; i < options.size(); i++) {
            Map<String,Object> opt = options.get(i);
            QuestionOption o = new QuestionOption();
            o.setQuestionId(questionId);
            o.setOptionLabel(String.valueOf((char)('A' + i)));
            o.setOptionText(opt.get("text").toString());
            o.setIsCorrect(opt.get("isCorrect") != null && Boolean.parseBoolean(opt.get("isCorrect").toString()) ? 1 : 0);
            o.setSortOrder(i);
            optionMapper.insert(o);
        }
    }

    private String typeName(int t) { switch(t) { case 1: return "单选题"; case 2: return "多选题"; case 3: return "判断题"; case 4: return "简答题"; default: return "未知"; } }
    private String diffName(int d) { switch(d) { case 1: return "简单"; case 2: return "中等"; case 3: return "困难"; default: return "未知"; } }
}
