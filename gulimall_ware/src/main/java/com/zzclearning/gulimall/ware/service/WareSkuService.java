package com.zzclearning.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.gulimall.ware.entity.WareSkuEntity;
import com.zzclearning.gulimall.ware.vo.StockLockVo;
import com.zzclearning.to.SkuHaStockVo;
import com.zzclearning.to.mq.StockLockedTo;
import com.zzclearning.vo.SkuLockStockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 22:03:50
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    Boolean updateWareSku(Long skuId, Integer skuNum, Long wareId);

    List<SkuHaStockVo> getStock(List<Long> skuIds);

    void orderLockStock(SkuLockStockVo skuLockStockVo);

    void handleOrderLock(String orderSn);

    void handleStockLock(StockLockedTo stockLockedTo);

    void handleOrderFinish(String orderSn);
}

