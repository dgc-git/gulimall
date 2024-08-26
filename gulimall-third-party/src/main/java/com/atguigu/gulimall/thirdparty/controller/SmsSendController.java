package com.atguigu.gulimall.thirdparty.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dgc
 * @date 2024/8/26 10:51
 */
@RestController
@RequestMapping("/sms")
public class SmsSendController {
    @Autowired
    SmsComponent smsComponent;
    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("phone") String phone,@RequestParam("code") String code){
        smsComponent.sendCode(phone,code);
        return R.ok();
    }
}
