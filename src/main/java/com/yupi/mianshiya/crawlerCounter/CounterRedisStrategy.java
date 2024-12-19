/**
 * copyright (C), 2015-2024
 * fileName: CounterManager
 *
 * @author: mlt
 * date:    2024/12/19 下午2:34
 * description:
 * history:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 * adpost    2024/12/19 下午2:34           V1.0
 */
package com.yupi.mianshiya.crawlerCounter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.IntegerCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 *
 * @author mlt
 * @version 1.0.0
 * @date 2024/12/19
 */
@Slf4j
@Component
public class CounterRedisStrategy implements CounterStrategy {
    @Resource
    private RedissonClient redissonClient;
    private final ConcurrentHashMap<String, Long> localAccessCount = new ConcurrentHashMap<>();
//    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//
//    public CounterRedisStrategy() {
//        // 启动定时任务，定时将本地统计的数据推送到 Redis
//        scheduler.scheduleAtFixedRate(this::pushLocalToRedis, 0, 1, TimeUnit.MINUTES);
//    }

    public Long doCount(String key, Integer timeInterval){
        return doCount(key, timeInterval, TimeUnit.MINUTES);
    }

    public Long doCount(String key, Integer timeInterval, TimeUnit timeUnit){
        Integer expireTime=timeInterval;
        switch (timeUnit){
            case SECONDS:
                expireTime=1;
                break;
            case MINUTES:
                expireTime=60;
                break;
            case HOURS:
                expireTime=60*60;
                break;
            default:
                throw new IllegalArgumentException("不支持的时间间隔");
        }
        return doCount(key,timeInterval,timeUnit,expireTime);
    }


    public Long doCount(String key, Integer timeInterval, TimeUnit timeUnit,Integer expireTime){
        if(StringUtils.isBlank(key)){
            return null;
        }
        Long timeStamp = System.currentTimeMillis();
        switch (timeUnit){
            case SECONDS:
                timeStamp = System.currentTimeMillis()/1000/timeInterval;
                break;
            case MINUTES:
                timeStamp = System.currentTimeMillis()/1000/timeInterval/60;
                break;
            case HOURS:
                timeStamp = System.currentTimeMillis()/1000/timeInterval/60/60;
                break;
            default:
                throw new IllegalArgumentException("不支持的时间间隔");
        }
        String countKey=key+":"+timeStamp;
        String luaScript =
                "if redis.call('exists', KEYS[1]) == 1 then " +
                        "  return redis.call('incr', KEYS[1]); " +
                        "else " +
                        "  redis.call('set', KEYS[1], 1); " +
                        "  redis.call('expire', KEYS[1], 180); " +  // 设置 180 秒过期时间
                        "  return 1; " +
                        "end";

        RScript script = redissonClient.getScript(IntegerCodec.INSTANCE);
        Object countObj = script.eval(
                RScript.Mode.READ_WRITE,
                luaScript,
                RScript.ReturnType.INTEGER,
                Collections.singletonList(countKey),
                expireTime
        );
        return (long)countObj;
    }

//    public Long doCount(String key, Integer timeInterval, TimeUnit timeUnit, Integer expireTime) {
//        if (StringUtils.isBlank(key)) {
//            return null;
//        }
//
//        Long timeStamp = System.currentTimeMillis();
//        switch (timeUnit){
//            case SECONDS:
//                timeStamp = System.currentTimeMillis()/1000/timeInterval;
//                break;
//            case MINUTES:
//                timeStamp = System.currentTimeMillis()/1000/timeInterval/60;
//                break;
//            case HOURS:
//                timeStamp = System.currentTimeMillis()/1000/timeInterval/60/60;
//                break;
//            default:
//                throw new IllegalArgumentException("不支持的时间间隔");
//        }
//        String countKey = key + ":" + timeStamp;
//
//        // 在本地统计
//        localAccessCount.merge(countKey, 1L, Long::sum);
//
//        // 返回本地统计结果
//        return localAccessCount.get(countKey);
//    }
//
//    // 定时推送本地统计到 Redis
//    private void pushLocalToRedis() {
//        if (localAccessCount.isEmpty()) {
//            return;
//        }
//
//        // 批量推送到 Redis
//        for (Map.Entry<String, Long> entry : localAccessCount.entrySet()) {
//            String countKey = entry.getKey();
//            Long count = entry.getValue();
//
//            // 使用 Lua 脚本进行原子操作，递增并设置过期时间
//            String luaScript =
//                    "if redis.call('exists', KEYS[1]) == 1 then " +
//                            "  return redis.call('incrby', KEYS[1], ARGV[1]); " +
//                            "else " +
//                            "  redis.call('set', KEYS[1], ARGV[1]); " +
//                            "  redis.call('expire', KEYS[1], ARGV[2]); " +  // 设置过期时间
//                            "  return ARGV[1]; " +
//                            "end";
//
//            RScript script = redissonClient.getScript(IntegerCodec.INSTANCE);
//            script.eval(
//                    RScript.Mode.READ_WRITE,
//                    luaScript,
//                    RScript.ReturnType.INTEGER,
//                    Collections.singletonList(countKey),
//                    count,  // 计数值
//                    180  // 过期时间（假设为60秒）
//            );
//        }
//
//        // 推送成功后清空本地统计
//        localAccessCount.clear();
//    }

}
