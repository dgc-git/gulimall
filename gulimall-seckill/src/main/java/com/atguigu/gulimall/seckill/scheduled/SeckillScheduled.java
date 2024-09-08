package com.atguigu.gulimall.seckill.scheduled;

import com.atguigu.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品定时上架
 * 每晚三点：上架最近三天需要秒杀的商品（提前预告）
 * 当天00：00：00-23：59：59
 * 明天00：00：00-23：59：59
 * 后天00：00：00-23：59：59
 */
@Service
@Slf4j
public class SeckillScheduled {
    @Autowired
    SeckillService seckillService;
    @Autowired
    RedissonClient redissonClient;
    private final String upload_lock = "seckill:upload:lock";

    @Scheduled(cron = "0 0 3 * * ?")
    @Async
    public void uploadSeckillSkuLateste3Days() {
        //重复上架无需处理
        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLateste3Days();
        } finally {
            lock.unlock();
        }
    }
}
