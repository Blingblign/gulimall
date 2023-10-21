package com.zzclearning.gulimall.ware.feign;

import com.zzclearning.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author bling
 * @create 2023-02-20 20:48
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {
    @GetMapping("order/order/orderInfo/{orderSn}")
    R getOrderInfo(@PathVariable("orderSn") String orderSn);
}
