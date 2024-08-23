package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson2.JSON;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dgc
 * @date 2024/8/22 11:42
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    private RestHighLevelClient client;

    @Override
    public SearchResult search(SearchParam param) {
        SearchResult result = null;
        //1.构建查询需要的dsl语句
        //1.构建searchRequest
        SearchRequest searchRequest = buildSearchRequest(param);
        //2.执行检索请求
        try {
            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
            //3.分析响应数据封装成需要的格式
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();
//        //1.返回的所有商品
        SearchHits hits = response.getHits();
        List<SkuEsModel> esModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (StringUtils.isNotEmpty(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);
//        //2.所有商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        if (attrIdAgg.getBuckets() != null) {
            for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
                SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
                //属性id
                long attrId = bucket.getKeyAsNumber().longValue();
                //属性名
                String attrName = ((ParsedStringTerms) (bucket.getAggregations().get("attr_name_agg"))).getBuckets().get(0).getKeyAsString();
                //属性值
                List<String> attrValues = ((ParsedStringTerms) (bucket.getAggregations().get("attr_value_agg"))).getBuckets().stream().map(item -> {
                    String keyAsString = item.getKeyAsString();
                    return keyAsString;
                }).collect(Collectors.toList());
                attrVo.setAttrId(attrId);
                attrVo.setAttrName(attrName);
                attrVo.setAttrValue(attrValues);
                attrVos.add(attrVo);
            }
        }
        result.setAttrs(attrVos);
//        //3.所有商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brandAgg = response.getAggregations().get("brand_agg");
        if (brandAgg.getBuckets() != null && brandAgg.getBuckets().size() > 0) {
            for (Terms.Bucket bucket : brandAgg.getBuckets()) {
                SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
                //品牌id
                long brandId = bucket.getKeyAsNumber().longValue();
                //品牌图片
                //todo需要判断是否为空
                String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
                //品牌名
                String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
                brandVo.setBrandId(brandId);
                brandVo.setBrandImg(brandImg);
                brandVo.setBrandName(brandName);
                brandVos.add(brandVo);
            }
        }

        result.setBrands(brandVos);
//        //3.所有商品涉及到的所有分类信息
        ParsedLongTerms catalogAgg = response.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalogAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            String keyAsString = bucket.getKeyAsString();
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVo.setCatalogId(Long.valueOf(keyAsString));
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);
//        ============以上从聚合信息中获取============
//        //3.分页信息
        result.setPageNum(param.getPageNum());
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        int totalPages = (int) (total + EsConstant.PRODUCT_PAGESIZE - 1) / EsConstant.PRODUCT_PAGESIZE;
        result.setTotalPages(totalPages);
        return result;
    }

    /**
     * 准备检索请求
     * 考虑：模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存），排序，分页，高亮，聚合分析
     *
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();//用于构建dsl语句
        //1.查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //1.1.must
        if (StringUtils.isNotEmpty(param.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        if (param.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        if (param.getHasStock() != null) {
            if (param.getHasStock() == 1) {
                //仅查询有库存的商品
                boolQueryBuilder.filter(QueryBuilders.termsQuery("hasStock", true));
            }
        }
        //属性
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            for (String attrStr : param.getAttrs()) {

                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                //attr=1_5寸:8寸
                String[] split = attrStr.split("_");
                String attrId = split[0];
                String[] attrValues = split[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                //每一个属性都是一个nestedQuery，都放入到大的filter里面
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQuery);
            }

        }
        //价格区间
        if (StringUtils.isNotEmpty(param.getSkuPrice())) {
            //1_500/_500/500_
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            //强制保留2位长度
            String[] split = param.getSkuPrice().split("_", -1);

            if (StringUtils.isNotEmpty(split[0])) {
                rangeQuery.gte(split[0]);
            } else {
                rangeQuery.gte(0);
            }
            if (StringUtils.isNotEmpty(split[1])) {
                rangeQuery.lte(split[1]);
            }
            boolQueryBuilder.filter(rangeQuery);
        }

        searchSourceBuilder.query(boolQueryBuilder);
        //2.排序，分页，高亮
        //排序
        if (StringUtils.isNotEmpty(param.getSort())) {
            String sort = param.getSort();
            //sort=hosScore_asc/desc
            String[] split = sort.split("_");
            SortOrder order = "asc".equalsIgnoreCase(split[1]) ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(split[0], order);
        }
        //分页

        searchSourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        //高亮
        if (StringUtils.isNotEmpty(param.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            searchSourceBuilder.highlighter(builder);
        }
        //3.聚合分析
        //品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        brandAgg.field("brandId").size(50);
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg.keyword").size(1));
        searchSourceBuilder.aggregation(brandAgg);
        //分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalogAgg);
        //属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        //聚合出属性id
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attrIdAgg);
        searchSourceBuilder.aggregation(attr_agg);
        System.out.println("构建的dsl语句：" + searchSourceBuilder.toString());
        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
    }
}
