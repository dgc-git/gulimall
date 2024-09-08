package com.atguigu.gulimall.seckill.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
public class SeckillController {
    /**
     * 返回当前时间参与秒杀的商品信息
     * @return
     */
    @Autowired
    SeckillService seckillService;
    @GetMapping("/getCurrentSeckillSkus")
    @ResponseBody
    public R getCurrentSeckillSkus(){
        List<SeckillSkuRedisTo> vos=seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }
    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId){
        SeckillSkuRedisTo to=seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(to);
    }
    @GetMapping("/kill")
    public String seckill(@NotBlank@RequestParam("killId") String killId,@NotBlank @RequestParam("key")String key,
                     @NotNull @RequestParam("num")Integer num,
                          Model model){
        //1.判断是否登录
        String orderSn=seckillService.kill(killId,key,num);
        model.addAttribute("orderSn",orderSn);
        return "success";

    }
}
