package com.zzclearning.gulimall.search.controller;

import com.zzclearning.common.exception.BizExceptionEnum;
import com.zzclearning.common.utils.R;
import com.zzclearning.gulimall.search.service.ProductSaveService;
import com.zzclearning.to.es.SkuEsModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author bling
 * @create 2023-02-03 21:15
 */
@RestController
@RequestMapping("/search/save")
public class ProductSaveController {
    @Autowired
    ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productSave(@RequestBody List<SkuEsModel> skuEsModels) {
        Boolean result;
        try {
            result = productSaveService.productSave(skuEsModels);
        } catch (IOException e) {
            return R.error(BizExceptionEnum.PRODUCT_UP_EXCEPTION.getCode(), BizExceptionEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }
        return result? R.ok() : R.error(BizExceptionEnum.PRODUCT_UP_EXCEPTION.getCode(), BizExceptionEnum.PRODUCT_UP_EXCEPTION.getMessage());
    }

}
