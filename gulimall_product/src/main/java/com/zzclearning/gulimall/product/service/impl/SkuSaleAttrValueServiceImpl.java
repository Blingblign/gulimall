package com.zzclearning.gulimall.product.service.impl;

import com.zzclearning.gulimall.product.vo.SkuItemVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.Query;

import com.zzclearning.gulimall.product.dao.SkuSaleAttrValueDao;
import com.zzclearning.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.zzclearning.gulimall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemVo.SaleAttrVo> getSkuItemSaleAttrs(Long spuId) {
        return baseMapper.getSkuItemSaleAttrs(spuId);
    }

    @Override
    public List<String> getSkuAttrvalues(Long skuId) {
        return baseMapper.getSkuAttrvalues(skuId);
    }

}