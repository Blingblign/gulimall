package com.zzclearning.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzclearning.common.utils.R;
import com.zzclearning.gulimall.product.constant.ProductConstant;
import com.zzclearning.gulimall.product.entity.*;
import com.zzclearning.gulimall.product.feign.CouponFeignService;
import com.zzclearning.gulimall.product.feign.SearchFeignService;
import com.zzclearning.gulimall.product.feign.WareFeignService;
import com.zzclearning.gulimall.product.service.*;
import com.zzclearning.gulimall.product.vo.Images;
import com.zzclearning.gulimall.product.vo.SpuInfoVo;
import com.zzclearning.to.MemberPrice;
import com.zzclearning.to.SkuHaStockVo;
import com.zzclearning.to.SkuReductionTo;
import com.zzclearning.to.SpuBoundsTo;
import com.zzclearning.to.es.SkuEsModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.Query;

import com.zzclearning.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import sun.rmi.runtime.Log;

import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;

@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    WareFeignService   wareFeignService;
    @Autowired
    SearchFeignService searchFeignService;
    @Autowired
    SpuImagesService   spuImagesService;
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    AttrService attrService;
    @Autowired
    ProductAttrValueService productAttrValueService;
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuSaleAttrValueService saleAttrValueService;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    BrandService brandService;
    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryByConditions(Map<String, Object> params) {
        LambdaQueryWrapper<SpuInfoEntity> wrapper = Wrappers.lambdaQuery();
        //按catelogId
        String catelogId = (String)params.get("catelogId");
        wrapper.eq(StringUtils.isNotBlank(catelogId),SpuInfoEntity::getCatalogId,catelogId);
        //按brandId
        String brandId = (String)params.get("brandId");
        wrapper.eq(StringUtils.isNotBlank(brandId),SpuInfoEntity::getBrandId,brandId);
        //按发布状态 status: "",
        String status = (String)params.get("status");
        wrapper.eq(StringUtils.isNotBlank(status),SpuInfoEntity::getPublishStatus,status);
        //按关键字
        String key = (String)params.get("key");
        wrapper.and(StringUtils.isNotBlank(key),item->{
            item.like(SpuInfoEntity::getId,key).or().like(SpuInfoEntity::getSpuName,key);
        });
        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }
    @Transactional
    @Override
    public Boolean saveSpuInfo(SpuInfoVo spuInfoVo) {
        //1.保存spu信息
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfoVo,spuInfoEntity);
        this.save(spuInfoEntity);
        //图片信息
        List<SpuImagesEntity> spuImagesEntities = spuInfoVo.getImages().stream().map(image -> {
            SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
            spuImagesEntity.setSpuId(spuInfoEntity.getId());
            spuImagesEntity.setImgUrl(image);
            return spuImagesEntity;
        }).collect(Collectors.toList());
        spuImagesService.saveBatch(spuImagesEntities);
        //商品描述
        List <SpuInfoDescEntity> spuInfoDescEntities = spuInfoVo.getDecript().stream().map(descript -> {
            SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
            spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
            spuInfoDescEntity.setDecript(descript);
            return spuInfoDescEntity;
        }).collect(Collectors.toList());
        spuInfoDescService.saveBatch(spuInfoDescEntities);
        //1.1.保存积分信息，远程调用coupon服务
        BigDecimal buyBounds = spuInfoVo.getBounds().getBuyBounds();
        BigDecimal growBounds = spuInfoVo.getBounds().getGrowBounds();
        if (!Objects.equals(buyBounds, new BigDecimal(0)) || !Objects.equals(growBounds, new BigDecimal(0))) {
            SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
            spuBoundsTo.setSpuId(spuInfoEntity.getId());
            spuBoundsTo.setBuyBounds(buyBounds);
            spuBoundsTo.setGrowBounds(growBounds);
            //优惠生效情况“1111”,转换成4个bit位
            String work = "1111";
            Integer decode = Integer.parseInt(work,2);
            log.info("优惠生效情况:{}",decode);
            spuBoundsTo.setWork(decode);
            R saveBoundsResult = couponFeignService.saveBounds(spuBoundsTo);
            //TODO R.ok()的code值没有用常数表示
            if (saveBoundsResult.getCode() != 0) {
                throw new RuntimeException("远程调用coupon服务保存积分信息出错");
            }
        }
        //1.2保存商品基本属性
        List <ProductAttrValueEntity> productAttrValueEntities = spuInfoVo.getBaseAttrs().stream().map(baseAttr -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            String attrName = attrService.getById(baseAttr.getAttrId()).getAttrName();
            productAttrValueEntity.setAttrName(attrName);
            productAttrValueEntity.setAttrId(baseAttr.getAttrId());
            productAttrValueEntity.setAttrValue(baseAttr.getAttrValues());
            productAttrValueEntity.setQuickShow(baseAttr.getShowDesc());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(productAttrValueEntities);
        //2. 保存sku信息
        spuInfoVo.getSkus().stream().forEach(skus->{
            //2.1 sku基本信息
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(skus,skuInfoEntity);
            skuInfoEntity.setSpuId(spuInfoEntity.getId());
            skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
            skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
            skuInfoEntity.setSaleCount(0L);
            for (Images image : skus.getImages()) {
                if (image.getDefaultImg() == 1 && !StringUtils.isBlank(image.getImgUrl())) {
                    skuInfoEntity.setSkuDefaultImg(image.getImgUrl());
                }
            }
            skuInfoService.save(skuInfoEntity);
            //2.2 sku-销售属性
            List <SkuSaleAttrValueEntity> skuSaleAttrValueEntities = skus.getAttr().stream().map(attr -> {
                SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                return skuSaleAttrValueEntity;
            }).collect(Collectors.toList());
            saleAttrValueService.saveBatch(skuSaleAttrValueEntities);
            //2.3 sku图片信息 ，filter：没有图片路径的无需保存
            List <SkuImagesEntity> skuImagesEntities = skus.getImages().stream().map(image -> {
                SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                BeanUtils.copyProperties(image, skuImagesEntity);
                skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                return skuImagesEntity;

            }).filter(skuImagesEntity -> {return !StringUtils.isBlank(skuImagesEntity.getImgUrl());}).collect(Collectors.toList());
            skuImagesService.saveBatch(skuImagesEntities);
            // 2.4 满减、折扣信息
            SkuReductionTo skuReductionTo = new SkuReductionTo();
            if (skus.getFullCount() > 0 || skus.getFullPrice().compareTo(new BigDecimal(0)) > 0) {
                skuReductionTo.setSkuId(skuInfoEntity.getSkuId());
                BeanUtils.copyProperties(skus,skuReductionTo);
                R result = couponFeignService.saveSkuReduction(skuReductionTo);
                if (result.getCode() != 0) {
                    throw new RuntimeException("远程调用，保存满减、折扣信息失败");
                }
            }
            // 2.5 会员价格信息

            List<MemberPrice> memberPrices = skus.getMemberPrice();
            List<MemberPrice> collect = memberPrices.stream().filter(ms -> {
                return ms.getPrice().compareTo(new BigDecimal(0)) > 0;
            }).peek(ms->ms.setSkuId(spuInfoEntity.getId())).collect(Collectors.toList());
            if (collect.size() > 0) {
                couponFeignService.saveMemberPrices(collect);
            }
        });
        return true;
    }

    @Override
    public void productUp(Long spuId) {
        //根据spu查询sku信息
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);
        //查询spu的规格属性
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.list(Wrappers.lambdaQuery(ProductAttrValueEntity.class).eq(ProductAttrValueEntity::getSpuId, spuId));
        //获得能够被用于检索的属性的集合
        List<Long> allIds = productAttrValueEntities.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        List<Long> searchIds = getSearchIds(allIds);
        List<SkuEsModel.Attrs> attrs = productAttrValueEntities.stream().filter(item -> searchIds.contains(item.getAttrId())).map(productAttrValueEntity -> {
            SkuEsModel.Attrs attr = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(productAttrValueEntity, attr);
            return attr;
        }).collect(Collectors.toList());
        //远程查询库存信息
        List<Long> skuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        R res = wareFeignService.skusHasStock(skuIds);
        Map<Long, Integer> stockMap = res.getData(new TypeReference<List<SkuHaStockVo>>() {
        }).stream().collect(Collectors.toMap(SkuHaStockVo::getSkuId, SkuHaStockVo::getStock));
        //封装skuEsModel
        List<SkuEsModel> skuEsModels = skuInfoEntities.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku,skuEsModel);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImage(sku.getSkuDefaultImg());
            //属性信息
            skuEsModel.setAttrs(attrs);
            //库存信息
            Integer skuStock = stockMap.get(sku.getSkuId());
            skuEsModel.setHasStock(skuStock != null && skuStock > 0);
            skuEsModel.setHotScore(0l);
            //查询品牌名、品牌图片
            Long brandId = sku.getBrandId();
            BrandEntity brandEntity = brandService.getById(brandId);
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImage(brandEntity.getLogo());
            //查询分类名
            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            skuEsModel.setCatalogName(categoryEntity.getName());

            return skuEsModel;
        }).collect(Collectors.toList());


        //2.远程调用elasticSearch存储数据
        R r = searchFeignService.productSave(skuEsModels);
        if (r.getCode() == 0) {
            //3.修改spu状态为已上架
            SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
            spuInfoEntity.setId(spuId);
            spuInfoEntity.setPublishStatus(ProductConstant.StatusEnum.SPU_UP.getCode());
            baseMapper.updateById(spuInfoEntity);
        } else {
            //远程调用失败
            //TODO 重复调用？接口幂等性
        }

    }

    private List<Long> getSearchIds(List<Long> allIds) {
        return baseMapper.getSearchIds(allIds);
    }

}