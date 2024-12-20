package com.yupi.mianshiya.aop;

import com.yupi.mianshiya.annotation.AuthCheck;
import com.yupi.mianshiya.annotation.CrawlerDetect;
import com.yupi.mianshiya.common.ErrorCode;
import com.yupi.mianshiya.crawlerCounter.CounterManager;
import com.yupi.mianshiya.exception.BusinessException;
import com.yupi.mianshiya.model.entity.User;
import com.yupi.mianshiya.model.enums.UserRoleEnum;
import com.yupi.mianshiya.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 权限校验 AOP
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Aspect
@Component
@Order(2)
public class CrawlerDetectInterceptor {

    @Resource
    private UserService userService;
    @Resource
    private CounterManager counterManager;

    /**
     * 执行拦截
     *
     * @param crawlerDetect
     * @return
     */
    @Before("@annotation(crawlerDetect)")
    public void doInterceptor(CrawlerDetect crawlerDetect) throws Throwable {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        User loginUser = userService.getLoginUser(request);
        counterManager.crawlerDetect(loginUser.getId());
    }
}

