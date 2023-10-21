package com.zzclearning.gulimall.order.feign;

import com.zzclearning.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author bling
 * @create 2023-02-20 9:27
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {
    @PostMapping("product/skuinfo/orderItemInfo")
    R getOrderItemInfoBySkuIds(@RequestBody List<Long> skuIds);
}
