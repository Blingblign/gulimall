package com.zzclearning.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.zzclearning.gulimall.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 16:44:33
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取sku所有销售属性
     * @param skuIds
     * @return
     */
    List<SkuItemVo.SaleAttrVo> getSkuItemSaleAttrs(Long skuIds);

    List<String> getSkuAttrvalues(Long skuId);
}

