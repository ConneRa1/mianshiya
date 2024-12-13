package com.yupi.mianshiya.esdao;

import com.yupi.mianshiya.model.dto.question.QuestionEsDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QuestionEsDaoTest {
    @Autowired
    private QuestionEsDao questionEsDao;

    @Test
    void findByUserId() {
        QuestionEsDTO questionEsDTO = new QuestionEsDTO();
        questionEsDTO.setId(1L);
        questionEsDTO.setTitle("");
        questionEsDTO.setContent("");
        questionEsDTO.setAnswer("");
        questionEsDTO.setTags(null);
        questionEsDTO.setUserId(1L);
        questionEsDTO.setCreateTime(null);
        questionEsDTO.setUpdateTime(null);
        questionEsDTO.setIsDelete(null);

        QuestionEsDTO save = questionEsDao.save(questionEsDTO);
        List<QuestionEsDTO> dtos = questionEsDao.findByUserId(1L);
        assertFalse(dtos.isEmpty());
    }
}