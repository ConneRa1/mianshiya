package com.yupi.mianshiya.crawlerCounter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
@Component
public class CounterLocalStrategy implements CounterStrategy{
    // 用于存储每个用户的访问次数计数器
    private ConcurrentHashMap<String, LongAdder> userRequestCounts = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public CounterLocalStrategy() {
        // 启动定时任务，定时将本地统计的数据推送到 Redis
        scheduler.scheduleAtFixedRate(this::startResetTask, 0, 60, TimeUnit.SECONDS);
    }

    /**
     * 每次用户访问时调用此方法
     * @param userId 用户的唯一标识符
     */
    public void recordRequest(String userId) {
        // 获取或者初始化用户的访问计数器
        userRequestCounts.computeIfAbsent(userId, key -> new LongAdder()).increment();
    }

    /**
     * 获取用户的当前访问次数
     * @param userId 用户的唯一标识符
     * @return 用户的访问次数
     */
    public long getRequestCount(String userId) {
        return userRequestCounts.getOrDefault(userId, new LongAdder()).sum();
    }

    /**
     * 定期重置每个用户的访问计数器
     */
    private void startResetTask() {
        // 定时任务，每隔指定的时间间隔重置计数
        new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(60);  // 等待指定的时间间隔
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                // 重置每个用户的计数器
                userRequestCounts.clear();
            }
        }).start();
    }

    @Override
    public Long doCount(String key, Integer timeInterval, TimeUnit timeUnit,Integer expireTime) {
        recordRequest(key);
        return getRequestCount(key);
    }
}
