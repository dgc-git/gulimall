package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @author dgc
 * @date 2024/8/22 11:40
 */
@Data
public class SearchParam {
    private String keyword;
    private Long catalog3Id;//三级分类id
    private String sort;//排序条件：saleCount_asc/desc ;skuprice;hotScore
    /**
     * hasStock
     *  skuPrice
     *  brandId
     *  catalog3Id
     *  attrs
     */
    private Integer hasStock;
    private String skuPrice;
    private List<Long> brandId;
    private List<String> attrs;
    private Integer pageNum;
}
