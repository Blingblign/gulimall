package com.zzclearning.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.zzclearning.gulimall.search.config.GulimallESconfig;
import com.zzclearning.gulimall.search.constant.EsConstant;
import com.zzclearning.gulimall.search.service.ProductSaveService;
import com.zzclearning.to.es.SkuEsModel;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 保存商品信息到es
 * @author bling
 * @create 2023-02-06 12:41
 */
@Slf4j
@Service("productSaveService")
public class ProductSaveServiceImpl implements ProductSaveService {
    @Autowired
    RestHighLevelClient client;

    @Override
    public Boolean productSave(List<SkuEsModel> skuEsModels) throws IOException {
        //进行批量保存
        BulkRequest bulkRequest = new BulkRequest();
        skuEsModels.forEach(skuEsModel -> {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            //设置id
            indexRequest.id(skuEsModel.getSkuId().toString());
            // 修改索引结构，使之可以按照拼音搜索；添加字段suggestion，自动补全
            //品牌名、cpu型号自动补全
            List<String> collect = skuEsModel.getAttrs().stream().map(SkuEsModel.Attrs::getAttrValue).collect(Collectors.toList());
            skuEsModel.getSuggestion().add(skuEsModel.getBrandName());
            skuEsModel.getSuggestion().addAll(collect);
            log.info("自动补全信息：{}",skuEsModel.getSuggestion().toString());
            //设置json串格式数据
            indexRequest.source(JSON.toJSONString(skuEsModel), XContentType.JSON);
            bulkRequest.add(indexRequest);
        });
        // 4、执行：同步
        BulkResponse bulk = client.bulk(bulkRequest, GulimallESconfig.COMMON_OPTIONS);//将异常往上抛
        boolean b = bulk.hasFailures();

        return !b;
    }
    //TODO 商品增删改，同步修改es中数据
}
