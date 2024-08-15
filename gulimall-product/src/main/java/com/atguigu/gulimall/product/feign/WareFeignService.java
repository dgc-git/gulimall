package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * ClassName: WareFeignService
 * Package: com.atguigu.gulimall.product.feign
 * Description:
 *
 * @Author: 邓桂材
 * @Create: 2024/8/15 -23:24
 * @Version: v1.0
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {
    @PostMapping("/hasstock")
    public R<List<SkuHasStockVo>> getSkuHasStock(@RequestBody List<Long> skuIds);
}
