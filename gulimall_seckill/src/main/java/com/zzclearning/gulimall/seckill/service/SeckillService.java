package com.zzclearning.gulimall.seckill.service;

import com.zzclearning.to.seckill.SeckillSessionWithSkusTo;
import com.zzclearning.to.seckill.SeckillSkuInfoTo;

import java.util.List;

/**
 * @author bling
 * @create 2023-02-25 15:41
 */
public interface SeckillService {
    List<SeckillSessionWithSkusTo> getRecentThreeDaysSessionWithSkus();

    List<SeckillSkuInfoTo> getCurrentSeckillSkus();

    SeckillSkuInfoTo getSkuSeckillInfo(Long skuId);

    void killSku(String killId, String key, Integer num);
}
