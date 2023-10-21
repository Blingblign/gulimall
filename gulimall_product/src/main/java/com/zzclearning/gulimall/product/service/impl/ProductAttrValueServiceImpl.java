package com.zzclearning.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.Query;

import com.zzclearning.gulimall.product.dao.ProductAttrValueDao;
import com.zzclearning.gulimall.product.entity.ProductAttrValueEntity;
import com.zzclearning.gulimall.product.service.ProductAttrValueService;
import org.springframework.transaction.annotation.Transactional;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }
    @Transactional
    @Override
    public Boolean updateSpuAttrs(List<ProductAttrValueEntity> productAttrValues, Long spuId) {
        //删除表中的spu规格
        this.remove(Wrappers.lambdaQuery(ProductAttrValueEntity.class).eq(ProductAttrValueEntity::getSpuId,spuId));
        //重新添加
        productAttrValues.forEach(item->item.setSpuId(spuId));
        this.saveBatch(productAttrValues);
        return true;

    }

}