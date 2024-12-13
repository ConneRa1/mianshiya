/**
 * copyright (C), 2015-2024
 * fileName: QuestionBankQuestionQuerBatchAddRequest
 *
 * @author: mlt
 * date:    2024/12/12 下午6:03
 * description:
 * history:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 * adpost    2024/12/12 下午6:03           V1.0
 */
package com.yupi.mianshiya.model.dto.questionBankQuestion;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 *
 *
 * @author mlt
 * @version 1.0.0
 * @date 2024/12/12
 */
@Data
public class QuestionBankQuestionBatchAddRequest implements Serializable {

    /**
     * 题库 id
     */
    private Long questionBankId;

    /**
     * 题目 id
     */
    private List<Long> questionId;

    private static final long serialVersionUID = 1L;

}
