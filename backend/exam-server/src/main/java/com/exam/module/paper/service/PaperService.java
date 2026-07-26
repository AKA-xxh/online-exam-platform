package com.exam.module.paper.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.exception.BusinessException;
import com.exam.common.result.ErrorCode;
import com.exam.common.result.PageResult;
import com.exam.module.paper.entity.*;
import com.exam.module.paper.mapper.*;
import com.exam.module.question.entity.Question;
import com.exam.module.question.entity.QuestionOption;
import com.exam.module.question.mapper.QuestionMapper;
import com.exam.module.question.mapper.QuestionOptionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaperService {

    private final ExamPaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper optionMapper;

    // ============ 试卷 CRUD ============
    public PageResult<Map<String,Object>> getPage(String keyword, Integer paperType, int page, int pageSize) {
        LambdaQueryWrapper<ExamPaper> qw = Wrappers.lambdaQuery(ExamPaper.class)
                .eq(paperType != null, ExamPaper::getPaperType, paperType)
                .like(keyword != null, ExamPaper::getTitle, keyword)
                .orderByDesc(ExamPaper::getCreateTime);
        Page<ExamPaper> mp = paperMapper.selectPage(new Page<>(page, pageSize), qw);
        List<Map<String,Object>> recs = mp.getRecords().stream().map(p -> {
            Map<String,Object> m = new HashMap<>();
            m.put("id", p.getId()); m.put("title", p.getTitle()); m.put("totalScore", p.getTotalScore());
            m.put("passScore", p.getPassScore()); m.put("duration", p.getDuration());
            m.put("paperType", p.getPaperType()); m.put("status", p.getStatus());
            m.put("createTime", p.getCreateTime());
            return m;
        }).collect(Collectors.toList());
        return new PageResult<>(recs, mp.getTotal(), page, pageSize, mp.getPages());
    }

    public Map<String,Object> getDetail(Long id) {
        ExamPaper paper = paperMapper.selectById(id);
        if (paper == null) throw new BusinessException(ErrorCode.PAPER_NOT_FOUND);
        Map<String,Object> m = new HashMap<>();
        m.put("paper", paper);
        m.put("questions", paperQuestionMapper.selectList(
                Wrappers.lambdaQuery(PaperQuestion.class).eq(PaperQuestion::getPaperId, id).orderByAsc(PaperQuestion::getSortOrder)));
        return m;
    }

    @Transactional
    public ExamPaper create(Map<String,Object> data) {
        ExamPaper p = buildPaper(data);
        paperMapper.insert(p);
        if (Integer.valueOf(1).equals(data.get("paperType"))) {
            // 手动组卷：questions 是题目ID列表，含分值
            saveManualQuestions(p.getId(), data);
        } else {
            // 随机组卷：保存规则并生成预览
            p.setGenRules(JSONUtil.toJsonStr(data.get("genRules")));
            paperMapper.updateById(p);
            generateRandom(p.getId(), data);
        }
        updateTotalScore(p.getId());
        return p;
    }

    @Transactional
    public void update(Long id, Map<String,Object> data) {
        ExamPaper p = paperMapper.selectById(id);
        if (p == null) throw new BusinessException(ErrorCode.PAPER_NOT_FOUND);
        ExamPaper updated = buildPaper(data);
        updated.setId(id);
        paperMapper.updateById(updated);
        paperQuestionMapper.delete(Wrappers.lambdaQuery(PaperQuestion.class).eq(PaperQuestion::getPaperId, id));
        if (Integer.valueOf(1).equals(data.get("paperType"))) {
            saveManualQuestions(id, data);
        } else {
            generateRandom(id, data);
        }
        updateTotalScore(id);
    }

    public void delete(Long id) {
        paperMapper.deleteById(id);
        paperQuestionMapper.delete(Wrappers.lambdaQuery(PaperQuestion.class).eq(PaperQuestion::getPaperId, id));
    }

    @Transactional
    public Map<String,Object> regenerate(Long id) {
        ExamPaper paper = paperMapper.selectById(id);
        if (paper == null) throw new BusinessException(ErrorCode.PAPER_NOT_FOUND);
        paperQuestionMapper.delete(Wrappers.lambdaQuery(PaperQuestion.class).eq(PaperQuestion::getPaperId, id));
        Map<String,Object> data = new HashMap<>();
        data.put("genRules", JSONUtil.parseArray(paper.getGenRules()));
        generateRandom(id, data);
        updateTotalScore(id);
        return getDetail(id);
    }

    // ============ 内部方法 ============
    private ExamPaper buildPaper(Map<String,Object> data) {
        ExamPaper p = new ExamPaper();
        p.setTitle(data.get("title").toString());
        p.setDescription(data.get("description") != null ? data.get("description").toString() : "");
        p.setTotalScore(data.get("totalScore") != null ? Integer.valueOf(data.get("totalScore").toString()) : 100);
        p.setPassScore(data.get("passScore") != null ? Integer.valueOf(data.get("passScore").toString()) : 60);
        p.setDuration(data.get("duration") != null ? Integer.valueOf(data.get("duration").toString()) : 60);
        p.setPaperType(data.get("paperType") != null ? Integer.valueOf(data.get("paperType").toString()) : 1);
        p.setShowAnswer(data.get("showAnswer") != null ? Integer.valueOf(data.get("showAnswer").toString()) : 1);
        p.setShuffleQuestion(data.get("shuffleQuestion") != null ? Integer.valueOf(data.get("shuffleQuestion").toString()) : 0);
        p.setShuffleOption(data.get("shuffleOption") != null ? Integer.valueOf(data.get("shuffleOption").toString()) : 0);
        p.setMaxScreenSwitches(data.get("maxScreenSwitches") != null ? Integer.valueOf(data.get("maxScreenSwitches").toString()) : 3);
        p.setStatus(0);
        return p;
    }

    @SuppressWarnings("unchecked")
    private void saveManualQuestions(Long paperId, Map<String,Object> data) {
        List<Map<String,Object>> questions = (List<Map<String,Object>>) data.get("questions");
        if (questions == null) return;
        for (int i = 0; i < questions.size(); i++) {
            Map<String,Object> qi = questions.get(i);
            Long qid = Long.valueOf(qi.get("questionId").toString());
            Question q = questionMapper.selectById(qid);
            if (q == null) continue;
            List<QuestionOption> opts = optionMapper.selectList(
                    Wrappers.lambdaQuery(QuestionOption.class).eq(QuestionOption::getQuestionId, qid).orderByAsc(QuestionOption::getSortOrder));
            PaperQuestion pq = new PaperQuestion();
            pq.setPaperId(paperId);
            pq.setQuestionId(qid);
            pq.setQuestionType(q.getQuestionType());
            pq.setSectionName(qi.get("sectionName") != null ? qi.get("sectionName").toString() : null);
            pq.setTitleSnapshot(q.getTitle());
            pq.setOptionsSnapshot(JSONUtil.toJsonStr(opts));
            pq.setAnswerSnapshot(buildAnswerSnapshot(q.getQuestionType(), opts));
            pq.setAnalysisSnapshot(q.getAnalysis());
            pq.setScore(qi.get("score") != null ? Integer.valueOf(qi.get("score").toString()) : 1);
            pq.setSortOrder(i);
            paperQuestionMapper.insert(pq);
        }
    }

    @SuppressWarnings("unchecked")
    private void generateRandom(Long paperId, Map<String,Object> data) {
        List<Map<String,Object>> rules;
        if (data.get("genRules") instanceof List) {
            rules = (List<Map<String,Object>>) data.get("genRules");
        } else if (data.get("genRules") instanceof String) {
            rules = (List) (Object) JSONUtil.toList((String) data.get("genRules"), Map.class);
        } else {
            return;
        }
        int order = 0;
        for (Map<String,Object> rule : rules) {
            Integer qType = Integer.valueOf(rule.get("questionType").toString());
            Long catId = rule.get("categoryId") != null ? Long.valueOf(rule.get("categoryId").toString()) : null;
            Integer diff = rule.get("difficulty") != null ? Integer.valueOf(rule.get("difficulty").toString()) : null;
            int count = Integer.valueOf(rule.get("count").toString());
            int score = Integer.valueOf(rule.get("scorePerQuestion").toString());
            String sectionName = rule.get("sectionName") != null ? rule.get("sectionName").toString() : null;

            LambdaQueryWrapper<Question> qw = Wrappers.lambdaQuery(Question.class)
                    .eq(Question::getQuestionType, qType)
                    .eq(Question::getStatus, 1)
                    .eq(catId != null, Question::getCategoryId, catId)
                    .eq(diff != null, Question::getDifficulty, diff)
                    .last("ORDER BY RAND() LIMIT " + count);
            List<Question> questions = questionMapper.selectList(qw);

            for (Question q : questions) {
                List<QuestionOption> opts = optionMapper.selectList(
                        Wrappers.lambdaQuery(QuestionOption.class).eq(QuestionOption::getQuestionId, q.getId()).orderByAsc(QuestionOption::getSortOrder));
                PaperQuestion pq = new PaperQuestion();
                pq.setPaperId(paperId); pq.setQuestionId(q.getId()); pq.setQuestionType(q.getQuestionType());
                pq.setSectionName(sectionName); pq.setTitleSnapshot(q.getTitle());
                pq.setOptionsSnapshot(JSONUtil.toJsonStr(opts));
                pq.setAnswerSnapshot(buildAnswerSnapshot(q.getQuestionType(), opts));
                pq.setAnalysisSnapshot(q.getAnalysis());
                pq.setScore(score); pq.setSortOrder(order++);
                paperQuestionMapper.insert(pq);
            }
        }
    }

    private String buildAnswerSnapshot(Integer questionType, List<QuestionOption> opts) {
        if (questionType == 4) return JSONUtil.toJsonStr(Collections.singletonList("简答题-参考答案"));
        List<String> corrects = opts.stream().filter(o -> o.getIsCorrect() == 1)
                .map(QuestionOption::getOptionLabel).collect(Collectors.toList());
        return JSONUtil.toJsonStr(corrects);
    }

    private void updateTotalScore(Long paperId) {
        List<PaperQuestion> pqs = paperQuestionMapper.selectList(
                Wrappers.lambdaQuery(PaperQuestion.class).eq(PaperQuestion::getPaperId, paperId));
        int total = pqs.stream().mapToInt(PaperQuestion::getScore).sum();
        ExamPaper p = new ExamPaper(); p.setId(paperId); p.setTotalScore(total);
        paperMapper.updateById(p);
    }
}
