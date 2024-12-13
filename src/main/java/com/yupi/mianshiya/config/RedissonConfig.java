/**
 * copyright (C), 2015-2024
 * fileName: RedissonConfig
 *
 * @author: mlt
 * date:    2024/12/12 下午2:23
 * description: redisson config
 * history:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 * adpost    2024/12/12 下午2:23           V1.0        redisson config
 */
package com.yupi.mianshiya.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redisson config
 *
 * @author mlt
 * @version 1.0.0
 * @date 2024/12/12
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {
    /**
     * host
     */
    private String host;

    /**
     * port
     */
    private Integer port;

    /**
     * db
     */
    private Integer database;

    /**
     * timeout
     */
    private Integer timeout;

    @Bean
    RedissonClient redisson() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://"+host+":"+port)
                .setDatabase(database)
                .setTimeout(timeout);
        return Redisson.create(config);
    }

}
