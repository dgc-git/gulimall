package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * ClassName: SkuItemSaleAttrVo
 * Package: com.atguigu.gulimall.product.vo
 * Description:
 *
 * @Author: 邓桂材
 * @Create: 2024/8/25 -10:48
 * @Version: v1.0
 */
@Data
public class SkuItemSaleAttrVo {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVO> attrValues;
}
