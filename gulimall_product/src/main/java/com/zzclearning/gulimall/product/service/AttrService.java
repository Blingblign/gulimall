package com.zzclearning.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.gulimall.product.entity.AttrEntity;
import com.zzclearning.gulimall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 16:44:33
 */
public interface AttrService extends IService<AttrEntity> {
    /**
     * 获取分类规格参数
     */
    PageUtils queryPage(Map<String, Object> params, Long catelogId, String attrType);

    /**
     * 查询attgroup未关联的属性id
     * @param params
     * @param attrIds 已关联属性id
     * @return
     */
    PageUtils queryPage(Map<String, Object> params, List<Long> noAttrIds);

    /**
     * 查询没有该分组下没有被关联的其他属性
     * @param params
     * @param attrGroupId
     * @return
     */
    PageUtils queryNoRelationAttr(Map<String, Object> params, Long attrGroupId);

    /**
     * 查询spu所有属性
     * @param spuId
     * @return
     */
    List<ProductAttrValueEntity> listAttrForSpu(Long spuId);
}

