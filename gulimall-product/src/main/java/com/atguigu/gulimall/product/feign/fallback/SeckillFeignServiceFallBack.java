package com.atguigu.gulimall.product.feign.fallback;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author dgc
 * @date 2024/9/9 14:09
 */
@Slf4j
@Component
public class SeckillFeignServiceFallBack implements SeckillFeignService {
    @Override
    public R getSkuSeckillInfo(Long skuId) {
        log.info("熔断方法调用...");
        return R.error(BizCodeEnum.TO0_MANY_REQUEST.ordinal(), BizCodeEnum.TO0_MANY_REQUEST.getMessage());
    }
}
