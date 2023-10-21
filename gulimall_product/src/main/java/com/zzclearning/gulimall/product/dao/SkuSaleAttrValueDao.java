package com.zzclearning.gulimall.product.dao;

import com.zzclearning.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzclearning.gulimall.product.vo.SkuItemVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 16:44:33
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuItemVo.SaleAttrVo> getSkuItemSaleAttrs(@Param("spuId") Long spuId);

    List<String> getSkuAttrvalues(@Param("skuId") Long skuId);
}
