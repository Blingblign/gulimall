package com.zzclearning.gulimall.cart.feign;

import com.zzclearning.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author bling
 * @create 2023-02-14 11:08
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {
    /**
     * 信息
     */
    @GetMapping("/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);

    /**
     * 获取销售属性列表 颜色：星河银
     */
    @GetMapping("/product/skusaleattrvalue/getSkuAttrvalues/{skuId}")
    List<String> getSkuAttrvalues(@PathVariable("skuId") Long skuId);
}
