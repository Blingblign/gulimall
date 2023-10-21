package com.zzclearning.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.zzclearning.gulimall.search.config.GulimallESconfig;
import com.zzclearning.gulimall.search.constant.EsConstant;
import com.zzclearning.gulimall.search.service.SearchService;
import com.zzclearning.gulimall.search.vo.SearchParamsVo;
import com.zzclearning.gulimall.search.vo.SearchResult;
import com.zzclearning.to.es.SkuEsModel;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bling
 * @create 2023-02-06 21:09
 */
@Service("searchService")
public class SearchServiceImpl implements SearchService {
    @Autowired
    RestHighLevelClient client;
    @Override
    public SearchResult lookUpEs(SearchParamsVo params) throws IOException {
        //构造检索请求
        SearchRequest searchRequest = buildSearchRequest(params);
        //获得检索结果
        SearchResponse searchResponse = client.search(searchRequest, GulimallESconfig.COMMON_OPTIONS);
        //解析检索结果
        SearchResult searchResult = buildSearchResult(searchResponse,params);
        return searchResult;
    }

    @Override
    public List<String> getSuggestion(String keyWord) {
        //自定义suggestion名，类似agg
        String suggestName = "text_suggestion";
        SearchRequest searchRequest = new SearchRequest(EsConstant.PRODUCT_INDEX);
        CompletionSuggestionBuilder suggestion = SuggestBuilders.completionSuggestion("suggestion").size(10).skipDuplicates(true).text(keyWord);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion(suggestName, suggestion);
        searchRequest.source().suggest(suggestBuilder);

        try {
            SearchResponse suggestionResp = client.search(searchRequest, RequestOptions.DEFAULT);
            List<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> suggestEntries =
                    suggestionResp.getSuggest().getSuggestion(suggestName).getEntries();
            List<String> collect = suggestEntries.get(0).getOptions().stream().map(option -> option.getText().toString()).collect(Collectors.toList());
            return collect;
        } catch (IOException e) {
            e.printStackTrace();

        }
        return null;
    }

    private SearchResult buildSearchResult(SearchResponse searchResponse,SearchParamsVo params) {
        SearchResult searchResult = new SearchResult();
        SearchHits hits = searchResponse.getHits();
        //分页数据
        long totalRecords = hits.getTotalHits().value;
        Long totalPage = (totalRecords + (EsConstant.PRODUCT_PAGESIZE - 1)) / EsConstant.PRODUCT_PAGESIZE;
        searchResult.setTotalRecords(totalRecords);
        searchResult.setTotalPage(totalPage);
        searchResult.setPageNum(params.getPageNum());
        ArrayList<Integer> pageNavs = new ArrayList<>();
        for(int i = 1; i <= totalPage; i ++) {
            pageNavs.add(i);
        }
        searchResult.setPageNavs(pageNavs);
        //获得商品数据
        List<SkuEsModel> products = Arrays.stream(hits.getHits()).map(hit -> {
            String skuString = hit.getSourceAsString();
            SkuEsModel skuEsModel = JSON.parseObject(skuString, SkuEsModel.class);
            //对关键词进行高亮显示
            if (!StringUtils.isEmpty(params.getKeyword())) {
                Text[] skuTitles = hit.getHighlightFields().get("skuTitle").getFragments();
                skuEsModel.setSkuTitle(skuTitles[0].toString());
            }
            return skuEsModel;
        }).collect(Collectors.toList());

        searchResult.setProducts(products);
        Aggregations aggregations = searchResponse.getAggregations();
        //分类聚合信息
        ParsedLongTerms catalog_agg = aggregations.get("catalog_agg");
        List<SearchResult.CatalogVo> catalogs = catalog_agg.getBuckets().stream().map(bucket -> {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            catalogVo.setCatalogId(Long.parseLong(bucket.getKeyAsString()));
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            return catalogVo;
        }).collect(Collectors.toList());
        searchResult.setCatalogs(catalogs);
        //品牌聚合信息
        ParsedLongTerms brand_agg = aggregations.get("brand_agg");
        List<SearchResult.BrandVo> brands = brand_agg.getBuckets().stream().map(bucket -> {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            brandVo.setBrandId(Long.parseLong(bucket.getKeyAsString()));
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            String brandImage = brand_img_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImage(brandImage);
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brand_name_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            return brandVo;
        }).collect(Collectors.toList());
        searchResult.setBrands(brands);
        //属性聚合信息
        ParsedNested attr_agg = aggregations.get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        List<SearchResult.AttrVo> attrs = attr_id_agg.getBuckets().stream().map(bucket -> {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            attrVo.setAttrId(Long.parseLong(bucket.getKeyAsString()));
            ParsedStringTerms attr_name_agg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attr_name_agg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);
            ParsedStringTerms attr_value_agg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = attr_value_agg.getBuckets().stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
            attrVo.setAttrValues(attrValues);
            return attrVo;
        }).collect(Collectors.toList());
        searchResult.setAttrs(attrs);
        //TODO 面包屑导航功能
        return searchResult;
    }

    private SearchRequest buildSearchRequest(SearchParamsVo searchParamsVo) {
        //构造检索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(searchParamsVo.getKeyword())) {
            //关键词检索
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", searchParamsVo.getKeyword()));
        }
        if (searchParamsVo.getCatalog3Id() != null) {
            //分类检索
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId",searchParamsVo.getCatalog3Id()));
        }
        if (searchParamsVo.getBrandId() != null && searchParamsVo.getBrandId().size() > 0) {
            //品牌检索
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",searchParamsVo.getBrandId()));
        }
        if (searchParamsVo.getHasStock() != null) {
            //是否有库存
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock",searchParamsVo.getHasStock() == 1));
        }
        if (!StringUtils.isEmpty(searchParamsVo.getSkuPrice())) {
            //价格区间检索500_5000, 500_, _5000
            String skuPrice = searchParamsVo.getSkuPrice();
            String[] prices = skuPrice.split("_");
            if (prices.length == 2) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("skuPrice")
                                                .gte(prices[0]).lte(prices[1]));
            } else if (skuPrice.startsWith("_")) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("skuPrice").lte(prices[0]));//new BigDecimal(prices[0]))
            } else {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("skuPrice").gte(prices[0]));
            }
        }
        if (searchParamsVo.getAttrs() != null && searchParamsVo.getAttrs().size()>0) {
            //规格属性检索 attrs=1_3G:4G:5G&attrs=2_骁龙845&attrs=4_高清屏
            for (String attrs : searchParamsVo.getAttrs()) {
                String[] s = attrs.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("attrs.attrId",attrId))
                        .must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));
                boolQueryBuilder.filter(QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None));
            }
        }
        sourceBuilder.query(boolQueryBuilder);
        /**
         * 排序，分页，高亮
         */
        if (!StringUtils.isEmpty(searchParamsVo.getSort())) {
            //排序 sort=saleCount_desc/asc
            String[] s = searchParamsVo.getSort().split("_");
            sourceBuilder.sort(s[0],"asc".equalsIgnoreCase(s[1]) ? SortOrder.ASC : SortOrder.DESC);
        }
        //分页
        sourceBuilder.from((searchParamsVo.getPageNum()-1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        //高亮
        if (!StringUtils.isEmpty(searchParamsVo.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle").preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");

            sourceBuilder.highlighter(highlightBuilder);
        }
        //执行聚合操作
        sourceBuilder.aggregation(AggregationBuilders.terms("brand_agg").field("brandId").size(50)
                                          .subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1))
                                          .subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImage").size(1)));
        sourceBuilder.aggregation(AggregationBuilders.terms("catalog_agg").field("catalogId").size(50)
                                          .subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1)));
        NestedAggregationBuilder nested = AggregationBuilders.nested("attr_agg", "attrs");
        nested.subAggregation(AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(50)
                                      .subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1))
                                      .subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50)));
        sourceBuilder.aggregation(nested);
        System.out.println("构造的检索dsl为："+sourceBuilder);
        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
    }
}
