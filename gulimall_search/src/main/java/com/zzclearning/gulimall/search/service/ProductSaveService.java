package com.zzclearning.gulimall.search.service;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.zzclearning.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author bling
 * @create 2023-02-06 12:41
 */
public interface ProductSaveService {
    Boolean productSave(List<SkuEsModel> skuEsModels) throws IOException;
}

