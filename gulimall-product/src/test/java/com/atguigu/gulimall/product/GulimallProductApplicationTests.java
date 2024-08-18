package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.service.CategoryService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@SpringBootTest(classes = GulimallProductApplication.class)

@Slf4j
class GulimallProductApplicationTests {
    @Autowired
    private CategoryService categoryService;

    @Test
    public void teseFindPath() {
//        Long[] catelogPath = categoryService.findCatelogPath(225L);
//        log.info("完整路径：{}", Arrays.asList(catelogPath));
    }
}



