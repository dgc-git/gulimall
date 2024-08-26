package com.atguigu.gulimall.auth.controller;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.ThirdPartFeignService;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author dgc
 * @date 2024/8/26 9:17
 */
@Controller
public class LoginController {
    @Autowired
    ThirdPartFeignService thirdPartFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping("/sms/sendcode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone){
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (StringUtils.isNotEmpty(redisCode)) {
            long lastTime = Long.parseLong(redisCode.split("_")[1]);
            if(System.currentTimeMillis()-lastTime<60*1000){
                //60s内不能再发
                return R.error(BizCodeEnum.VALID_SMS_CODE_EXCEPTION.getCode(),BizCodeEnum.VALID_SMS_CODE_EXCEPTION.getMessage());
            }
        }
        //接口防刷

        //验证码再次校验
        //sms:code:1828034xxxx ->code
        String code = UUID.randomUUID().toString().substring(0, 5)+"_"+System.currentTimeMillis();
        //redis缓存验证码
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,code,10, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(phone,code);
        return R.ok();
    }
    @PostMapping("/regist")
    public String regist(@Valid @RequestParam UserRegistVo vo, BindingResult result, Model model){
        if(result.hasErrors()){
//            .map(fieldError -> {
//                String field = fieldError.getField();
//                String defaultMessage = fieldError.getDefaultMessage();
//                errors.put(field,defaultMessage);
//                return
//            })
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage));
            model.addAttribute("errors",errors);
            return "forward:/reg.html";
        }

        //注册成功回到登录页
        return "redirect:/login.html";
    }
}

