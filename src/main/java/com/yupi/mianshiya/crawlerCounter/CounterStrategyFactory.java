package com.yupi.mianshiya.crawlerCounter;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CounterStrategyFactory {
    private final CounterRedisStrategy counterRedisStrategy;
    private final CounterLocalStrategy counterLocalStrategy;

    public CounterStrategy getStrategy(String strategyType) {
        switch (strategyType) {
            case "redis":
                return counterRedisStrategy;
            case "local":
                return counterLocalStrategy;
            default:
                return counterRedisStrategy;  // 默认策略
        }
    }
}
