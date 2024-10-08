package com.atguigu.gulimall.search.controller;

import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * @author dgc
 * @date 2024/8/22 11:23
 */
@Controller
public class SearchController {
    @Autowired
    MallSearchService mallSearchService;
    @GetMapping({"/", "/list.html"})
    public String listPage(SearchParam param, Model model, HttpServletRequest request){
        String queryString = request.getQueryString();
        param.set_queryString(queryString);
        SearchResult result=mallSearchService.search(param);
        System.out.println("result"+result);
        model.addAttribute("result",result);
        return "list";
    }
}
