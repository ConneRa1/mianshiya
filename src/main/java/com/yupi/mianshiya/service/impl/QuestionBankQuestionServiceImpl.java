package com.yupi.mianshiya.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.mianshiya.annotation.AuthCheck;
import com.yupi.mianshiya.common.BaseResponse;
import com.yupi.mianshiya.common.ErrorCode;
import com.yupi.mianshiya.common.ResultUtils;
import com.yupi.mianshiya.constant.CommonConstant;
import com.yupi.mianshiya.constant.UserConstant;
import com.yupi.mianshiya.exception.BusinessException;
import com.yupi.mianshiya.exception.ThrowUtils;
import com.yupi.mianshiya.mapper.QuestionBankQuestionMapper;
import com.yupi.mianshiya.model.dto.questionBankQuestion.*;
import com.yupi.mianshiya.model.entity.Question;
import com.yupi.mianshiya.model.entity.QuestionBank;
import com.yupi.mianshiya.model.entity.QuestionBankQuestion;
import com.yupi.mianshiya.model.entity.User;
import com.yupi.mianshiya.model.vo.QuestionBankQuestionVO;
import com.yupi.mianshiya.model.vo.UserVO;
import com.yupi.mianshiya.service.QuestionBankQuestionService;
import com.yupi.mianshiya.service.QuestionBankService;
import com.yupi.mianshiya.service.QuestionService;
import com.yupi.mianshiya.service.UserService;
import com.yupi.mianshiya.utils.SqlUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 题库题目服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Service
@Slf4j
@AllArgsConstructor
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion> implements QuestionBankQuestionService {

    private final UserService userService;
    private final QuestionBankService questionBankService;
    private final QuestionService questionService;

    /**
     * 校验数据
     *
     * @param questionBankQuestion
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestionBankQuestion(QuestionBankQuestionAddRequest questionBankQuestion, boolean add) {
        ThrowUtils.throwIf(questionBankQuestion == null, ErrorCode.PARAMS_ERROR);
        // 题目和题库必须存在
        Long questionId = questionBankQuestion.getQuestionId();
        if (questionId != null) {
            Question question = questionService.getById(questionId);
            ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        Long questionBankId = questionBankQuestion.getQuestionBankId();
        if (questionBankId != null) {
            QuestionBank questionBank = questionBankService.getById(questionBankId);
            ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR, "题库不存在");
        }
    }

    @Override
    public BaseResponse<Long> add(QuestionBankQuestionAddRequest questionBankQuestionAddRequest)
    {
        validQuestionBankQuestion(questionBankQuestionAddRequest, true);
        QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
        questionBankQuestion.setQuestionId(questionBankQuestionAddRequest.getQuestionId());
        questionBankQuestion.setQuestionBankId(questionBankQuestionAddRequest.getQuestionBankId());
        boolean save = super.save(questionBankQuestion);
        if(!save){
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR.getCode(), "新增题库题目失败");
        }
        return ResultUtils.success(questionBankQuestion.getQuestionId());

    }

    @Override
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> remove(QuestionBankQuestionRemoveRequest questionBankQuestionRemoveRequest)
    {
        validQuestionBankQuestion(questionBankQuestionRemoveRequest, true);
        LambdaQueryWrapper<QuestionBankQuestion> queryWrapper = new LambdaQueryWrapper<QuestionBankQuestion>();
        queryWrapper.eq(QuestionBankQuestion::getQuestionId, questionBankQuestionRemoveRequest.getQuestionId());
        queryWrapper.eq(QuestionBankQuestion::getQuestionBankId, questionBankQuestionRemoveRequest.getQuestionBankId());
        boolean save = super.remove(queryWrapper);
        if(!save){
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR.getCode(), "新增题库题目失败");
        }
        return ResultUtils.success(save);

    }


    /**
     * 获取查询条件
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest) {
        QueryWrapper<QuestionBankQuestion> queryWrapper = new QueryWrapper<>();
        if (questionBankQuestionQueryRequest == null) {
            return queryWrapper;
        }

        String sortField = questionBankQuestionQueryRequest.getSortField();
        String sortOrder = questionBankQuestionQueryRequest.getSortOrder();
        Long questionBankId = questionBankQuestionQueryRequest.getQuestionBankId();
        Long questionId = questionBankQuestionQueryRequest.getQuestionId();
        Long userId = questionBankQuestionQueryRequest.getUserId();
        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionBankId), "questionBankId", questionBankId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);

        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题库题目封装
     *
     * @param questionBankQuestion
     * @param request
     * @return
     */
    @Override
    public QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion questionBankQuestion, HttpServletRequest request) {
        // 对象转封装类
        QuestionBankQuestionVO questionBankQuestionVO = QuestionBankQuestionVO.objToVo(questionBankQuestion);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = questionBankQuestion.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionBankQuestionVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        // endregion

        return questionBankQuestionVO;
    }

    /**
     * 分页获取题库题目封装
     *
     * @param questionBankQuestionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> questionBankQuestionPage, HttpServletRequest request) {
        List<QuestionBankQuestion> questionBankQuestionList = questionBankQuestionPage.getRecords();
        Page<QuestionBankQuestionVO> questionBankQuestionVOPage = new Page<>(questionBankQuestionPage.getCurrent(), questionBankQuestionPage.getSize(), questionBankQuestionPage.getTotal());
        if (CollUtil.isEmpty(questionBankQuestionList)) {
            return questionBankQuestionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionBankQuestionVO> questionBankQuestionVOList = questionBankQuestionList.stream().map(questionBankQuestion -> {
            return QuestionBankQuestionVO.objToVo(questionBankQuestion);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        // endregion

        questionBankQuestionVOPage.setRecords(questionBankQuestionVOList);
        return questionBankQuestionVOPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAddQuestionInner(List<QuestionBankQuestion> addList) {
        try{
            boolean result = super.saveBatch(addList);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
            return result;
        }
        catch (DataIntegrityViolationException e) {
            log.error("数据库唯一键冲突或违反其他完整性约束, 错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目已存在于该题库，无法重复添加");
        } catch (DataAccessException e) {
            log.error("数据库连接问题、事务问题等导致操作失败, 错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据库操作失败");
        } catch (Exception e) {
            // 捕获其他异常，做通用处理
            log.error("添加题目到题库时发生未知错误，错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
        }
    }





    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchRemoveQuestionInner(QuestionBankQuestionBatchRemoveRequest questionBankQuestionBatchRemoveRequest) {
        Long questionBankId = questionBankQuestionBatchRemoveRequest.getQuestionBankId();
        List<Long> questionIds = questionBankQuestionBatchRemoveRequest.getQuestionId();
        // 校验题库id
        ThrowUtils.throwIf(questionIds == null || questionIds.isEmpty(),ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(questionBankId==null,ErrorCode.PARAMS_ERROR);
        return super.removeByIds(questionIds);
    }

    @Override
    // 分批处理
    public Boolean batchAddQuestion(QuestionBankQuestionBatchAddRequest questionBankQuestionBatchAddRequest) {
        Long questionBankId = questionBankQuestionBatchAddRequest.getQuestionBankId();
        List<Long> questionId = questionBankQuestionBatchAddRequest.getQuestionId();
        int batchSize=500;
        Boolean flag=true;
        //自定义线程池
        ThreadPoolExecutor threadPoolExecutor=new ThreadPoolExecutor(
                20,
                50,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        //保存异步结果
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for(int i=0;i<questionId.size();i+=batchSize){
            List<Long> subList = questionId.subList(i,Math.min(i+batchSize,questionId.size()));
            questionBankQuestionBatchAddRequest.setQuestionId(subList);
            List<QuestionBankQuestion> addQuestions =
                    subList.stream().map(item->{
                        return QuestionBankQuestion.builder()
                                .questionId(item)
                                .questionBankId(questionBankId)
                                .build();
                    }).collect(Collectors.toList());
            // !!!这里去找到了当前的代理对象，不然只用this调用方法，事务不启用
            QuestionBankQuestionService questionBankQuestionService=
                    (QuestionBankQuestionService) AopContext.currentProxy();
            // 使用自定义线程池，并检测异常
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                questionBankQuestionService.batchAddQuestionInner(addQuestions);
            },threadPoolExecutor).exceptionally(ex->{
                log.warn("error:"+ex.getMessage());
                return null;
            });
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();
        // 关闭线程池
        threadPoolExecutor.shutdown();
        return flag;

    }

    @Override
    public Boolean batchRemoveQuestion(QuestionBankQuestionBatchRemoveRequest questionBankQuestionBatchRemoveRequest) {
        Long questionBankId = questionBankQuestionBatchRemoveRequest.getQuestionBankId();
        List<Long> questionIds = questionBankQuestionBatchRemoveRequest.getQuestionId();
        int batchSize=500;
        Boolean flag=true;
        for(int i=0;i<questionIds.size();i+=batchSize){
            List<Long> subList = questionIds.subList(i,Math.min(i+batchSize,questionIds.size()));
            questionBankQuestionBatchRemoveRequest.setQuestionId(subList);
            // !!! 这里去找到了当前的代理对象，不然只用this调用方法，事务不启用
            QuestionBankQuestionService questionBankQuestionService=
                    (QuestionBankQuestionService) AopContext.currentProxy();
            flag = questionBankQuestionService.batchRemoveQuestionInner(questionBankQuestionBatchRemoveRequest);
        }
        return flag;
    }
}
