package com.zzclearning.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzclearning.gulimall.product.entity.CategoryBrandRelationEntity;
import com.zzclearning.gulimall.product.entity.CategoryEntity;
import com.zzclearning.gulimall.product.service.CategoryBrandRelationService;
import com.zzclearning.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.Query;

import com.zzclearning.gulimall.product.dao.BrandDao;
import com.zzclearning.gulimall.product.entity.BrandEntity;
import com.zzclearning.gulimall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    CategoryService categoryService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<BrandEntity> wrapper = Wrappers.lambdaQuery();
        //关键字检索
        String key = (String)params.get("key");
        if (!StringUtils.isBlank(key)) {
            wrapper.and(item->{
                item.like(BrandEntity::getBrandId,key)
                        .or().like(BrandEntity::getName,key)
                        //.or().like(AttrEntity::getIcon,key)
                        .or().like(BrandEntity::getDescript,key)
                        .or().like(BrandEntity::getFirstLetter,key);
            });
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public boolean batchSave(List<BrandEntity> brands, @Nullable Long catalogId) {
        BrandEntity one = this.getOne(Wrappers.query(brands.get(0)));
        if (one != null && one.getBrandId() != null) {
            throw new RuntimeException("请勿重复批量添加数据");
        }
        //批量新增品牌，关联分类
        this.saveBatch(brands);
        if (catalogId == null) {
            catalogId = 225L;//默认手机分类添加
        }
        CategoryEntity category = categoryService.getById(catalogId);
        List<CategoryBrandRelationEntity> categoryBrandRelationEntities = brands.stream().map(brandEntity -> {
            CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
            categoryBrandRelationEntity.setBrandId(brandEntity.getBrandId());
            categoryBrandRelationEntity.setBrandName(brandEntity.getName());
            categoryBrandRelationEntity.setCatelogId(category.getCatId());
            categoryBrandRelationEntity.setCatelogName(category.getName());
            return categoryBrandRelationEntity;
        }).collect(Collectors.toList());
        categoryBrandRelationService.saveBatch(categoryBrandRelationEntities);
        return true;
    }

}