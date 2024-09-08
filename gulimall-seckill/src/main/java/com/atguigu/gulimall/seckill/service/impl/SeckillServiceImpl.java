package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.atguigu.gulimall.seckill.vo.SeckillSkuVo;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.lettuce.core.RedisClient;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RabbitTemplate rabbitTemplate;


    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";//+商品随机码


    @Override
    //todo 上架秒杀商品的时候，每个数据都有过期时间，活动过期即过期
    //todo 秒杀后续的流程，简化了收货地址等信息
    public String kill(String killId, String key, Integer num) {
        MemberRespVo respVo = LoginUserInterceptor.loginUser.get();
        //获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String json = hashOps.get(killId);
        if (StringUtils.isEmpty(json)) {
            return null;
        } else {
            SeckillSkuRedisTo redis = JSON.parseObject(json, SeckillSkuRedisTo.class);
            //校验合法性
            Long startTime = redis.getStartTime();
            Long endTime = redis.getEndTime();
            long currentTime = new Date().getTime();
            long ttl = endTime - currentTime;
            //校验时间合法性
            if (currentTime >= startTime && currentTime <= endTime) {
                //校验随机码和商品id
                String skuIdWithSessionId = redis.getPromotionSessionId() + "_" + redis.getSkuId();
                String randomCode = redis.getRandomCode();
                if (key.equals(randomCode) && killId.equals(skuIdWithSessionId)) {
                    //验证购物数量是否合理
                    if (num <= redis.getSeckillLimit().intValue()) {
                        //验证这个认是否已经购买过，如果秒杀成功，就去占位：userId_SessionId_SkuId
                        String redisKey = respVo.getId() + "_" + skuIdWithSessionId;
                        //自动过期
                        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (aBoolean) {
                            //占位成功说明从来没买过
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                            boolean b = semaphore.tryAcquire(num);
                            if (b) {
                                //成功获取
                                //秒杀成功
                                //快速下单，给mq发消息
                                String timeId = null;
                                try {
                                    timeId = IdWorker.getTimeId();
                                    SeckillOrderTo orderTo = new SeckillOrderTo();
                                    orderTo.setOrderSn(timeId);
                                    orderTo.setMemberId(respVo.getId());
                                    orderTo.setNum(num);
                                    orderTo.setPromotionSessionId(redis.getPromotionSessionId());
                                    orderTo.setSkuId(redis.getSkuId());
                                    orderTo.setSeckillPrice(redis.getSeckillPrice());
                                    rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
                                } catch (AmqpException e) {
                                //获取到信号量之后如果发生异常需要增加信号量
                                    semaphore.release(num);
                                }
                                return timeId;
                            } else {
                                //释放占位
                                redisTemplate.delete(redisKey);
                            }

                        } else {
                            //已经买过，不能再买
                            return null;
                        }
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        //1.找到所有需要参与秒杀的商品的key信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            String regx = "\\d_" + skuId;
            for (String key : keys) {
                if (Pattern.matches(regx, key)) {
                    String json = hashOps.get(key);
                    SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
                    long current = new Date().getTime();
                    if (seckillSkuRedisTo == null) {
                        throw new RuntimeException("时间为空");
                    }
                    if (current >= seckillSkuRedisTo.getStartTime() && current <= seckillSkuRedisTo.getEndTime()) {

                    } else {
                        seckillSkuRedisTo.setRandomCode(null);
                    }
                    return seckillSkuRedisTo;
                }
            }
        }
        return null;
    }

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        //1.确定当前时间属于哪个秒杀场次
        //1970
        long time = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        for (String key : keys) {
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            long start = Long.parseLong(s[0]);
            long end = Long.parseLong(s[1]);
            if (time >= start && time <= end) {
                //2.获取这个秒杀场次所有商品信息
                List<String> range = redisTemplate.opsForList().range(key, 0, -1);
                //存的是string，返回的也是string。存的时候键是StringRedisSerializer，值是Json序列化方式
                BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<String> list = ops.multiGet(range);
                if (list != null && list.size() > 0) {
                    List<SeckillSkuRedisTo> collect = list.stream().map(item -> {
                        SeckillSkuRedisTo redisTo = JSON.parseObject((String) item, SeckillSkuRedisTo.class);
//                        redisTo.setRandomCode(null);//当前秒杀已经开始，需要随机码
                        return redisTo;
                    }).collect(Collectors.toList());
                    return collect;
                }
            }
        }
        return null;
    }

    @Override
    @Transactional
    public void uploadSeckillSkuLateste3Days() {
        //1.扫描需要秒杀的活动
        R sessions = couponFeignService.getLatest3DaySession();
        if (sessions.getCode() == 0) {
            //上架
            List<SeckillSessionsWithSkus> sessionData = sessions.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            //缓存到redis
            //1.缓存活动信息，主要是开始、结束时间
            saveSessionInfos(sessionData);
            //2.缓存活动关联的商品信息
            saveSessionSkuInfos(sessionData);
        }
    }

    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessions) {
        sessions.stream().forEach(session -> {
            Long startTime = session.getStartTime().getTime();
            Long endTime = session.getEndTime().getTime();
            String key = startTime + "_" + endTime + session.getId();//+id防重
            List<String> collect = session.getRelationSkus().stream().map(item -> item.getPromotionSessionId() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
            //缓存活动信息
            if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {//防止拆箱时空指针异常
                redisTemplate.opsForList().leftPushAll(SESSIONS_CACHE_PREFIX + key, collect);
            }

        });
    }


    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessions) {
        sessions.stream().forEach(session -> {
            //准备hash操作的结构
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelationSkus().stream().forEach(seckillSkuVo -> {
                //商品随机码
                if (Boolean.FALSE.equals(ops.hasKey(seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getId().toString()))) {

                    String token = UUID.randomUUID().toString().replace("-", "");
                    //没有商品就不用设置对应信号量了
                    //todo 信号量删除/过期？
                    //信号量+sessionid，避免三天类上架同一商品是出现信号量设置问题
                    //为了避免商品上架成功，信号量设置失败的问题，（trySetPermits不会抛异常），需要先设置信号量
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + session.getId() + token);
                    //商品秒杀件数作为信号量 限流
                    boolean b = semaphore.trySetPermits(seckillSkuVo.getSeckillCount().intValue());
                    if (!b) {
                        //todo 信号量设置失败的情况
                        throw new RuntimeException("秒杀商品库存设置失败");
                    }
                    //缓存商品
                    SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                    //1.sku基本数据
                    R skuInfo = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                    if (skuInfo.getCode() == 0) {
                        SkuInfoVo info = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        redisTo.setSkuInfo(info);
                    }
                    BeanUtils.copyProperties(seckillSkuVo, redisTo);
                    //2.sku的秒杀信息
                    //3.设置当前商品秒杀的时间信息
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());
                    redisTo.setRandomCode(token);
                    String jsonString = JSON.toJSONString(redisTo);
                    ops.put(seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getId().toString(), jsonString);
                    //todo 设置过期时间，后动结束就过期
                }

            });
        });
    }
}
