package com.atguigu.gulimall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定制任务
 * 1.@EnableScheduling
 */
//@EnableScheduling
@Component
@Slf4j
//@EnableAsync
public class HelloSchedule {

    /**
     * 1.spring中没有年份
     * 2.spring中周一到周日为1-7
     * 3.定时任务一般不应该阻塞。默认是阻塞的
     *      a.业务以异步方式执行
     *      b.spring支持定时任务线程池,默认线程池大小为一个线程，需要自己设置为多个，但是由于版本问题，不太好使，可以自己单独写线程池执行
     *      c.让定时任务异步执行
     *          @EnableAsync 开启异步任务
     *          @Async 给希望异步执行的方法标注
     *          异步任务本身和定时任务没有关系，任何一个业务都可以通过这样开启异步任务。
     *      解决定时任务阻塞的问题
     */

    @Async
    @Scheduled(cron="* * * * * ?")
    //todo 幂等性
    public void hello() throws InterruptedException {
        log.info("hello...");
        Thread.sleep(4000);
    }
}
