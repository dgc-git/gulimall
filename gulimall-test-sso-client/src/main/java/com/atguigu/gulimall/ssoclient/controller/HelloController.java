package com.atguigu.gulimall.ssoclient.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dgc
 * @date 2024/8/27 15:52
 */
@Controller
public class HelloController {
    /**
     * 无需登录
     * @return
     */
    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }
    /**
     * 需要登录
     */
    @GetMapping("/employees")
    public String employees(Model model, HttpSession session){
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            //没登录

        }
        List<String> emps=new ArrayList<>();
        emps.add("张三");
        emps.add("李四");
        model.addAttribute("emps",emps);
    return "list";
    }

}
