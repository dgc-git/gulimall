package com.atguigu.gulimall.search.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private List<Integer> pageNavs;
    private List<NavVo> navs=new ArrayList<>();
    private List<Long> attrIds=new ArrayList<>();

    public List<Integer> getPageNavs() {
        return pageNavs;
    }

    public void setPageNavs(List<Integer> pageNavs) {
        this.pageNavs = pageNavs;
    }

    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        private String link;
    }
    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }
    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }
    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
}
