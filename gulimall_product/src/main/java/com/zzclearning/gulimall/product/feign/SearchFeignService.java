package com.zzclearning.gulimall.product.feign;

import com.zzclearning.common.utils.R;
import com.zzclearning.to.es.SkuEsModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author bling
 * @create 2023-02-06 12:09
 */
@FeignClient("gulimall-search")
public interface SearchFeignService {
    @PostMapping("/search/save/product")
    R productSave(@RequestBody List<SkuEsModel> skuEsModels);
}
