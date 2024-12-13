package com.yupi.mianshiya.mapper;

import com.yupi.mianshiya.model.entity.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
* @author nihuo
* @description 针对表【question(题目)】的数据库操作Mapper
* @createDate 2024-12-10 17:14:43
* @Entity com.yupi.mianshiya.model.entity.Question
*/
public interface QuestionMapper extends BaseMapper<Question> {


    @Select("select * from question where updateTime> #{minUpdateTime} ")
    public List<Question> listQuestionWithDelete(@Param("minUpdateTime") Date minUpdateTime);
}




