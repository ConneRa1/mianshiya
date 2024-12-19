/**
 * copyright (C), 2015-2024
 * fileName: CountManager
 *
 * @author: mlt
 * date:    2024/12/19 下午4:29
 * description:
 * history:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 * adpost    2024/12/19 下午4:29           V1.0
 */
package com.yupi.mianshiya.crawlerCounter;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yupi.mianshiya.common.ErrorCode;
import com.yupi.mianshiya.exception.BusinessException;
import com.yupi.mianshiya.model.entity.User;
import com.yupi.mianshiya.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 *
 *
 * @author mlt
 * @version 1.0.0
 * @date 2024/12/19
 */
@Service
public class CounterManager {
    @Value("${crawler.warn-count:10}")
    private Integer WARN_COUNT;

    @Value("${crawler.ban-count:20}")
    private Integer BAN_COUNT;

    @Value("${crawler.detect-type}")
    private String DETECT_TYPE="redis";

    @Value("${crawler.interval: 60}")
    private Integer INTERVAL;

    @Value("${crawler.expire-time: 180}")
    private Integer EXPIRE_TIME;

    @Resource
    private UserService userService;
    @Resource
    private CounterStrategyFactory counterStrategyFactory;

    public void crawlerDetect(Long userId){
        CounterStrategy counterStrategy = counterStrategyFactory.getStrategy(DETECT_TYPE);

        String key="user:accsee:"+userId;
        Long count = counterStrategy.doCount(key,INTERVAL, TimeUnit.SECONDS,EXPIRE_TIME);
        if(count>BAN_COUNT){
            StpUtil.kickout(userId);
            LambdaUpdateWrapper<User> updateQuery=
                    new LambdaUpdateWrapper<User>().eq(User::getId,userId).set(User::getUserRole,"ban");
            userService.update(updateQuery);
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"鉴定为爬虫，封号中");
        }
        else if(count.intValue()==WARN_COUNT){
            // 给管理员发个邮件或微信
            throw new BusinessException(110,"初步检测到爬虫，警告！");
        }
    }
}
