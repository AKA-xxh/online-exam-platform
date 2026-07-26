package com.exam.module.question.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.module.question.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {}
