package com.atguigu.gulimall.search.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

/**
 * @author dgc
 * @date 2024/8/22 12:27
 */
@Data
public class SearchResult {
    //查询到的所有商品信息
    private List<SkuEsModel> products;
    private Integer pageNum;
    private Long total;
    private Integer totalPages;
    private List<BrandVo> brands;//当前查询到的结果所涉及到的所有品牌
    private List<CatalogVo> catalogs;//当前查询到的结果所涉及到的所有分类
    private List<AttrVo> attrs;//当前查询到的结果所涉及到的所有属性
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
}
