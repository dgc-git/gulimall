package com.atguigu.gulimall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author dgc
 * @date 2024/8/21 8:56
 */
@Configuration
public class MyRedissonConfig {
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson()throws IOException {
        Config config=new Config();
        config.useSingleServer()
                .setAddress("redis://192.168.238.100:6379");
        return Redisson.create(config);
    }
}
