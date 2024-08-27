package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.feign.ThirdPartFeignService;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
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

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/sms/sendCode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (StringUtils.isNotEmpty(redisCode)) {
            long lastTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - lastTime < 60 * 1000) {
                //60s内不能再发
                return R.error(BizCodeEnum.VALID_SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.VALID_SMS_CODE_EXCEPTION.getMessage());
            }
        }
        //接口防刷

        //验证码再次校验
        //sms:code:1828034xxxx ->code
        String substring = UUID.randomUUID().toString().substring(0, 5);
        String code = substring + "_" + System.currentTimeMillis();
        //redis缓存验证码
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code, 10, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(phone, substring);
        return R.ok();
    }

    @PostMapping("/register")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        System.out.println(System.currentTimeMillis());
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage, (existing, replacement) -> (String) existing));
//            model.addAttribute("errors",errors);
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        //注册逻辑
        String code = vo.getCode();
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (StringUtils.isNotEmpty(s)) {
            if (code.equals(s.split("_")[0])) {
                //删除验证码
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                R r = memberFeignService.regist(vo);
                if (r.getCode() == 0) {
                    return "redirect:http://auth.gulimall.com/login.html";

                } else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", (String) r.getData("msg", new TypeReference<String>() {
                    }));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
//            model.addAttribute("errors",errors);
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session) {
        //远程登录
        R r = memberFeignService.login(vo);
        if (r.getCode() == 0) {
            //成功
            MemberRespVo data = r.getData("data", new TypeReference<MemberRespVo>() {
            });
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
            return "redirect:http://gulimall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", r.getData("msg", new TypeReference<String>() {
            }));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session) {
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute == null) {
            return "login";
        }else {
            return "redirect:http://gulimall.com";
        }
    }
}

