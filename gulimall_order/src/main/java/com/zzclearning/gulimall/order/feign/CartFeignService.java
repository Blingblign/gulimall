package com.zzclearning.gulimall.order.feign;

import com.zzclearning.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * TODO feign的失败降级功能：注册fallbackFactory到容器中，在feignClient中指定fallbackFactory
 * @author bling
 * @create 2023-02-17 18:03
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {
    @GetMapping("/checkedItems")
    R getCheckedCartItemList();
}
