package com.zzclearning.gulimall.coupon.feign;

import com.zzclearning.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author bling
 * @create 2023-02-25 18:20
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {
    /**
     * 信息
     */
    @GetMapping("/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);
}
