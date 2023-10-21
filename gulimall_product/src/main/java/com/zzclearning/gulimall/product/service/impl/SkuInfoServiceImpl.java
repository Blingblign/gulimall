package com.zzclearning.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzclearning.common.utils.R;
import com.zzclearning.gulimall.product.entity.SkuImagesEntity;
import com.zzclearning.gulimall.product.entity.SpuInfoDescEntity;
import com.zzclearning.gulimall.product.entity.SpuInfoEntity;
import com.zzclearning.gulimall.product.feign.SeckillFeignService;
import com.zzclearning.gulimall.product.service.*;
import com.zzclearning.gulimall.product.vo.OrderItemTo;
import com.zzclearning.gulimall.product.vo.SkuItemVo;
import com.zzclearning.to.seckill.SeckillSkuInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.Query;

import com.zzclearning.gulimall.product.dao.SkuInfoDao;
import com.zzclearning.gulimall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    SkuImagesService        skuImagesService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    AttrGroupService attrGroupService;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    SpuInfoService spuInfoService;
    @Autowired
    SeckillFeignService seckillFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryByConditions(Map<String, Object> params) {
        LambdaQueryWrapper<SkuInfoEntity> wrapper = Wrappers.lambdaQuery();
        //按catelogId
        String catelogId = (String)params.get("catelogId");
        wrapper.eq(StringUtils.isNotBlank(catelogId) && !"0".equalsIgnoreCase(catelogId), SkuInfoEntity::getCatalogId,catelogId);
        //按brandId
        String brandId = (String)params.get("brandId");
        wrapper.eq(StringUtils.isNotBlank(brandId) && !"0".equalsIgnoreCase(brandId),SkuInfoEntity::getBrandId,brandId);
        //按关键字
        String key = (String)params.get("key");
        wrapper.and(StringUtils.isNotBlank(key),item->{
            item.like(SkuInfoEntity::getSkuId,key).or().like(SkuInfoEntity::getSkuName,key);
        });
        //按最小价格
        String minPrice = (String)params.get("min");
        wrapper.ge(StringUtils.isNotBlank(minPrice) && !"0".equalsIgnoreCase(minPrice),SkuInfoEntity::getPrice,minPrice);
        //按最大价格
        String maxPrice = (String)params.get("max");
        wrapper.le(StringUtils.isNotBlank(maxPrice) && !"0".equalsIgnoreCase(maxPrice),SkuInfoEntity::getPrice,maxPrice);
        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return baseMapper.selectList(Wrappers.lambdaQuery(SkuInfoEntity.class).eq(SkuInfoEntity::getSpuId,spuId));
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();
        //异步编排
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1.查询sku基本信息
            SkuInfoEntity skuInfo = this.getById(skuId);
            skuItemVo.setInfo(skuInfo);
            return skuInfo;
        }, threadPoolExecutor);
        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            //2.sku图片信息
            List<SkuImagesEntity> images = skuImagesService.list(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
            skuItemVo.setImages(images);
        }, threadPoolExecutor);
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((skuInfo) -> {
            //3.销售属性信息
            Long spuId = skuInfo.getSpuId();
            List<SkuItemVo.SaleAttrVo> saleAttrs = skuSaleAttrValueService.getSkuItemSaleAttrs(spuId);
            skuItemVo.setSaleAttr(saleAttrs);
        },threadPoolExecutor);
        CompletableFuture<Void> groupAttrFuture = infoFuture.thenAcceptAsync((skuInfo) -> {
            //4.spu属性分组带规格参数信息
            List<SkuItemVo.AttrGroupWithAttrsVo> groupAttrs = attrGroupService.getAttrGroupWithAttrs(skuInfo.getSpuId());
            skuItemVo.setGroupAttrs(groupAttrs);
        }, threadPoolExecutor);
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((skuInfo) -> {
            //5.spu介绍信息
            SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(skuInfo.getSpuId());
            skuItemVo.setDesc(spuInfoDesc);
        }, threadPoolExecutor);
        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            //查询商品秒杀信息
            R res = seckillFeignService.getSkuSeckillInfo(skuId);
            if (res.getCode() == 0) {
                SeckillSkuInfoTo data = res.getData(new TypeReference<SeckillSkuInfoTo>() {
                });
                skuItemVo.setSeckillSku(data);
            }
        }, threadPoolExecutor);
        //等待所有异步线程执行完
        CompletableFuture.allOf(imageFuture,saleAttrFuture,groupAttrFuture,descFuture,seckillFuture).get();
        return skuItemVo;
    }

    @Override
    public List<OrderItemTo> getOrderItemInfoBySkuIds(List<Long> skuIds) {
        List<SkuInfoEntity> skuInfoEntities = this.listByIds(skuIds);
        Map<Long, SkuInfoEntity> skuMap = skuInfoEntities.stream().collect(Collectors.toMap(SkuInfoEntity::getSkuId, skuInfoEntity -> skuInfoEntity));
        //批量查询spu信息
        List<Long> spuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSpuId).collect(Collectors.toList());
        Map<Long, SpuInfoEntity> spuInfoEntityMap = spuInfoService.listByIds(spuIds).stream().collect(Collectors.toMap(SpuInfoEntity::getId, spuInfoEntity -> spuInfoEntity));
        List<OrderItemTo> collect = skuIds.stream().map((id) -> {
            OrderItemTo orderItemTo = new OrderItemTo();
            SkuInfoEntity skuInfo = skuMap.get(id);
            orderItemTo.setSkuId(id);
            //查询sku的名字
            orderItemTo.setSkuName(skuInfo.getSkuName());
            //查询spu信息
            SpuInfoEntity spuInfoEntity = spuInfoEntityMap.get(skuInfo.getSpuId());
            orderItemTo.setSpuId(spuInfoEntity.getId());
            orderItemTo.setSpuName(spuInfoEntity.getSpuName());
            orderItemTo.setCategoryId(skuInfo.getCatalogId());
            return orderItemTo;
        }).collect(Collectors.toList());
        return collect;
    }


}