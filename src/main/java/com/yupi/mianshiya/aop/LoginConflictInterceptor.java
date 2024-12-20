package com.yupi.mianshiya.aop;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.Header;
import com.yupi.mianshiya.annotation.AuthCheck;
import com.yupi.mianshiya.annotation.LoginConflictCheck;
import com.yupi.mianshiya.common.ErrorCode;
import com.yupi.mianshiya.exception.BusinessException;
import com.yupi.mianshiya.model.entity.User;
import com.yupi.mianshiya.model.enums.UserRoleEnum;
import com.yupi.mianshiya.saToken.DeviceUtils;
import com.yupi.mianshiya.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.yupi.mianshiya.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 权限校验 AOP
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Aspect
@Component
@Order(1)
public class LoginConflictInterceptor {

    @Resource
    private UserService userService;
    @Resource
    private RedissonClient redissonClient;

    /**
     * 执行拦截
     *
     * @param loginConflictCheck
     * @return
     */
    @Before("@annotation(loginConflictCheck)")
    public void doInterceptor( LoginConflictCheck loginConflictCheck) throws Throwable {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        // 获取当前会话的唯一标识
        SaSession session = StpUtil.getSession();
        String userAgentStr = request.getHeader(Header.USER_AGENT.toString());
        User user = (User) session.get(USER_LOGIN_STATE);

        // 检查 Redis 是否有冲突记录
        RMap<String, String> userLoginConflict = redissonClient.getMap("user_login_conflict");
        String conflictAgent = userLoginConflict.get(String.valueOf(user.getId()));

        if (conflictAgent != null && !conflictAgent.equals(userAgentStr)) {
            if(DeviceUtils.getRequestDeviceFromStr(userAgentStr).equals(DeviceUtils.getRequestDeviceFromStr(conflictAgent))){
                userLoginConflict.remove(String.valueOf(user.getId()));
                StpUtil.logout(user.getId(),userAgentStr);
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"您已在另一设备登录，请重新登录");
            }
        }

    }
}

