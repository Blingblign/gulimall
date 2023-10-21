package com.zzclearning.gulimall.ware.dao;

import com.zzclearning.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzclearning.to.SkuHaStockVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 22:03:50
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void updateSkuNum(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum, @Param("wareId") Long wareId);

    List<SkuHaStockVo> getStock(@Param("skuIds") List<Long> skuIds);

    List<Long> skuWareHasStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);

    int updateStockLock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    void releaseStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    void reduceSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);
}
