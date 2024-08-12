package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ClassName: ProductFeignService
 * Package: com.atguigu.gulimall.ware.feign
 * Description:
 *
 * @Author: 邓桂材
 * @Create: 2024/8/13 -1:01
 * @Version: v1.0
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {
    /**
     * 1.给网关发请求：@FeignClient("gulimall-gateway") url:api/product/skuinfo/info/{skuId}
     *2.直接发给指定服务@FeignClient("gulimall-product") url:product/skuinfo/info/{skuId}
     * @param skuId
     * @return
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
