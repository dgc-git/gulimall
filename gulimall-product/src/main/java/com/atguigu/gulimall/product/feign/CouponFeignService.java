package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundsTo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ClassName: SpuFeignService
 * Package: com.atguigu.gulimall.product.feign
 * Description:
 *
 * @Author: 邓桂材
 * @Create: 2024/8/8 -22:41
 * @Version: v1.0
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    //只要json数据模型是兼容的，双方服务无需使用同一个to
    @PostMapping("coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    @PostMapping("coupon/skufullreduction/saveInfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
