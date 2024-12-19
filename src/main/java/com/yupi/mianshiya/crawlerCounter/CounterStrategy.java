/**
 * copyright (C), 2015-2024
 * fileName: CounterStrategy
 *
 * @author: mlt
 * date:    2024/12/19 下午4:07
 * description:
 * history:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 * adpost    2024/12/19 下午4:07           V1.0
 */
package com.yupi.mianshiya.crawlerCounter;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RScript;
import org.redisson.client.codec.IntegerCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 *
 *
 * @author mlt
 * @version 1.0.0
 * @date 2024/12/19
 */
public interface CounterStrategy {
    Long doCount(String key, Integer timeInterval, TimeUnit timeUnit,Integer expireTime);
}
