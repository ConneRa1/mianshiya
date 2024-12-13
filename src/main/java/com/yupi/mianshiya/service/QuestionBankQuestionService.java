package com.yupi.mianshiya.service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.mianshiya.common.BaseResponse;
import com.yupi.mianshiya.model.dto.questionBankQuestion.*;
import com.yupi.mianshiya.model.entity.QuestionBankQuestion;
import com.yupi.mianshiya.model.vo.QuestionBankQuestionVO;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 题库题目服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
public interface QuestionBankQuestionService extends IService<QuestionBankQuestion> {

    /**
     * 校验数据
     *
     * @param questionBankQuestion
     * @param add 对创建的数据进行校验
     */
    void validQuestionBankQuestion(QuestionBankQuestionAddRequest questionBankQuestion, boolean add);

    BaseResponse<Long> add(QuestionBankQuestionAddRequest questionBankQuestionAddRequest);

    BaseResponse<Boolean> remove(QuestionBankQuestionRemoveRequest questionBankQuestionRemoveRequest);

    /**
     * 获取查询条件
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest);
    
    /**
     * 获取题库题目封装
     *
     * @param questionBankQuestion
     * @param request
     * @return
     */
    QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion questionBankQuestion, HttpServletRequest request);

    /**
     * 分页获取题库题目封装
     *
     * @param questionBankQuestionPage
     * @param request
     * @return
     */
    Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> questionBankQuestionPage, HttpServletRequest request);

    @Transactional(rollbackFor = Exception.class)
    Boolean batchAddQuestionInner(List<QuestionBankQuestion> addQuestions);

    @Transactional(rollbackFor = Exception.class)
    Boolean batchRemoveQuestionInner(QuestionBankQuestionBatchRemoveRequest questionBankQuestionBatchRemoveRequest);

    // 分批处理
    Boolean batchAddQuestion(QuestionBankQuestionBatchAddRequest questionBankQuestionBatchAddRequest);

    Boolean batchRemoveQuestion(QuestionBankQuestionBatchRemoveRequest questionBankQuestionBatchRemoveRequest);
}
