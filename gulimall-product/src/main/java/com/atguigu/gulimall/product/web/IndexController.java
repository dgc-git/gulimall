package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * ClassName: IndexController
 * Package: com.atguigu.gulimall.product.web
 * Description:
 *
 * @Author: 邓桂材
 * @Create: 2024/8/18 -20:17
 * @Version: v1.0
 */
@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;
    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){
        //1.查出所有一级分类
        List<CategoryEntity> categoryEntities=categoryService.getLevel1Categorys();
        model.addAttribute("categorys",categoryEntities);
        //prefix：classpath:/templates/  suffix:.html
        return "index";
    }
    //index/catalog.json
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson(){
        return categoryService.getCatalogJson();
    }
}
