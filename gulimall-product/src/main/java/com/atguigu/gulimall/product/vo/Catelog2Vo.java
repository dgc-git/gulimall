package com.atguigu.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ClassName: Catelog2Vo
 * Package: com.atguigu.gulimall.product.vo
 * Description:
 *
 * @Author: 邓桂材
 * @Create: 2024/8/18 -20:34
 * @Version: v1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catelog2Vo {
    private String catalog1Id;//一级分类
    private List<catelog3Vo> catalog3List;//三级分类
    private String id;
    private String name;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class catelog3Vo{
        private String catalog2Id;//父分类，二级分类id
        private String id;
        private String name;
    }
}
