/**
 * copyright (C), 2015-2024
 * fileName: CacheNames
 *
 * @author: mlt
 * date:    2024/12/12 下午2:28
 * description:
 * history:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 * adpost    2024/12/12 下午2:28           V1.0
 */
package com.yupi.mianshiya.constant;

/**
 *
 *
 * @author mlt
 * @version 1.0.0
 * @date 2024/12/12
 */
public interface CacheNames {
    /**
     * 用户签到记录key
     * 格式：USER_SIGN_IN+年份+用户编号
     * 返回：用户该年的签到记录
     */
    String USER_SIGN_IN = "mianshiya:user:signs";

    static String getUserSignInKey(Integer year,Long userId) {
        return USER_SIGN_IN + ":" + year + ":" + userId;
    }
}
