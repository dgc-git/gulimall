package com.atguigu.gulimall.search.service.impl;

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
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.io.IOException;

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
            result = buildSearchResult(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private SearchResult buildSearchResult(SearchResponse response) {
        return null;
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

            if (param.getHasStock() == 1) {
                //仅查询有库存的商品
                boolQueryBuilder.filter(QueryBuilders.termsQuery("hasStock", true));
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
            //todo _500长度为2？
            //1_500/_500/500_
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] split = param.getSkuPrice().split("_");
            if (split.length == 2) {
                rangeQuery.gte(split[0]).lte(split[1]);
            } else if (split.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(split[0]);
                }
                if (param.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(split[0]);
                }
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
        System.out.println("构建的dsl语句：" + searchSourceBuilder.toString());
        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
    }
}
