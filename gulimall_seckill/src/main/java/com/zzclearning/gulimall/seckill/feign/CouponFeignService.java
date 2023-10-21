package com.zzclearning.gulimall.seckill.feign;

import com.zzclearning.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author bling
 * @create 2023-02-23 21:42
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    /**
     * 获取最近三天需要上架的活动和商品数据
     */
    @GetMapping("/schedule/list")
    R getRecentThreeDaysSessionsWithSkus();
}
