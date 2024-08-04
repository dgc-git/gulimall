package com.atguigu.gulimall.member.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ClassName: CouponFeignService
 * Package: com.atguigu.gulimall.member.feign
 * Description:
 *
 * @Author: 邓桂材
 * @Create: 2024/8/4 -18:25
 * @Version: v1.0
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    @RequestMapping("coupon/coupon/member/list")
    public R membercoupons();
}
