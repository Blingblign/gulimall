package com.zzclearning.gulimall.product.feign;

import com.zzclearning.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author bling
 * @create 2023-02-26 11:51
 */
@FeignClient("gulimall-seckill")
public interface SeckillFeignService {
    /**
     * 查看当前商品是否参与秒杀/秒杀预告
     * @return
     */
    @GetMapping("/seckill/{skuId}")
    R getSkuSeckillInfo(@PathVariable("skuId") Long skuId);
}
