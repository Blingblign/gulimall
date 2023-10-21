package com.zzclearning.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzclearning.gulimall.product.entity.BrandEntity;
import com.zzclearning.gulimall.product.entity.CategoryEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.Query;

import com.zzclearning.gulimall.product.dao.CategoryBrandRelationDao;
import com.zzclearning.gulimall.product.entity.CategoryBrandRelationEntity;
import com.zzclearning.gulimall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryBrandRelationEntity> getCategoryList(Long brandId) {
        LambdaQueryWrapper<CategoryBrandRelationEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CategoryBrandRelationEntity::getBrandId, brandId);
        return this.list(wrapper);
    }

    @Override
    public Boolean remove(Long[] ids,Class<?> clazz) {
        LambdaQueryWrapper<CategoryBrandRelationEntity> wrapper = Wrappers.lambdaQuery();
        if (clazz.isAssignableFrom(BrandEntity.class))
            wrapper.in(CategoryBrandRelationEntity::getBrandId, Arrays.asList(ids));
        else if (clazz.isAssignableFrom(CategoryEntity.class))
            wrapper.in(CategoryBrandRelationEntity::getCatelogId, Arrays.asList(ids));
        return this.remove(wrapper);
    }

    @Override
    public List<CategoryBrandRelationEntity> getBrandsByCatelogId(Long catelogId) {
        return this.list(Wrappers.lambdaQuery(CategoryBrandRelationEntity.class).eq(CategoryBrandRelationEntity::getCatelogId,catelogId));
    }


}