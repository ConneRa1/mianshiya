package com.yupi.mianshiya.aop;

import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import com.yupi.mianshiya.annotation.AuthCheck;
import com.yupi.mianshiya.annotation.HotKeyDetect;
import com.yupi.mianshiya.common.ErrorCode;
import com.yupi.mianshiya.common.ResultUtils;
import com.yupi.mianshiya.exception.BusinessException;
import com.yupi.mianshiya.model.entity.User;
import com.yupi.mianshiya.model.enums.UserRoleEnum;
import com.yupi.mianshiya.model.vo.QuestionBankVO;
import com.yupi.mianshiya.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * Hotkey校验 AOP
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Aspect
@Component
public class HotKeyInterceptor {

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param hotKeyDetect
     * @return
     */
    @Around("@annotation(hotKeyDetect)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, HotKeyDetect hotKeyDetect) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String keyPrefix = hotKeyDetect.KeyPrefix();
        // 获取 SpEL 表达式
        String spelKey = hotKeyDetect.Key();

        // 使用 Spring 的 SpEL 解析器解析 key
        EvaluationContext context = new StandardEvaluationContext();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = new LocalVariableTableParameterNameDiscoverer().getParameterNames(method);

        // 将方法参数添加到上下文中
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        // 使用 SpEL 解析 key
        ExpressionParser parser = new SpelExpressionParser();
        String key = parser.parseExpression(spelKey).getValue(context, String.class);

        String hotKey = keyPrefix+":"+key;
        if(JdHotKeyStore.isHotKey(hotKey)){
            Object vo = JdHotKeyStore.get(hotKey);
            if(vo!=null){
                return ResultUtils.success(vo);
            }
        }
        synchronized (this) {
            Object proceed = joinPoint.proceed();
            JdHotKeyStore.smartSet(hotKey,proceed);
            return ResultUtils.success(proceed);
        }
    }
}

