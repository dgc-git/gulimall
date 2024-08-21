package com.atguigu.gulimall.product.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author dgc
 * @date 2024/8/21 14:19
 */
@EnableConfigurationProperties(CacheProperties.class)//让原来和配置文件绑定的配置类生效
@Configuration
@EnableCaching
public class MyCacheConfig {

    @Autowired
    CacheProperties cacheProperties;
    @Bean
    RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties){
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
//        config=config.entryTtl();
        config=config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
        config=config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        //将配置文件中的配置生效
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        if(redisProperties.getTimeToLive()!=null){
            config=config.entryTtl(redisProperties.getTimeToLive());
        }
        if(redisProperties.getKeyPrefix()!=null){
            config=config.prefixCacheNameWith(redisProperties.getKeyPrefix());
        }
        if(!redisProperties.isCacheNullValues()){
            config=config.disableCachingNullValues();
        }
        if(!redisProperties.isUseKeyPrefix()){
            config=config.disableKeyPrefix();
        }
        return config;
    }
}
