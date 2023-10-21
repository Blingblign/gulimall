package com.zzclearning.gulimall.product.vo;

import com.zzclearning.gulimall.product.entity.SkuImagesEntity;
import com.zzclearning.gulimall.product.entity.SkuInfoEntity;
import com.zzclearning.gulimall.product.entity.SpuInfoDescEntity;
import com.zzclearning.to.seckill.SeckillSkuInfoTo;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 商品详情信息
 * @author bling
 * @create 2023-02-08 15:34
 */
@Data
public class SkuItemVo {
    //sku基本信息
    private SkuInfoEntity info;
    //是否有货
    private Boolean               hasStock = true;
    //sku图片信息
    private List<SkuImagesEntity> images;
    //sku销售属性信息
    List<SaleAttrVo> saleAttr;
    //spu属性分组带规格参数信息
    List<AttrGroupWithAttrsVo> groupAttrs;
    //spu介绍
    private SpuInfoDescEntity desc;
    //秒杀信息
    private SeckillSkuInfoTo seckillSku;

    @ToString
    @Data
    public static class SaleAttrVo {
        private Long attrId;
        private String attrName;
        private List<AttrWithSkusVo> attrValues;
    }
    //得到每个属性值所对应的skuIds
    @ToString
    @Data
    public static class AttrWithSkusVo {
        private String attrValue;
        private String skuIds;
    }

    //sku--> spuId,catalogId --> 分组信息，分组下的属性名，spu商品属性值
    @ToString
    @Data
    public static class AttrGroupWithAttrsVo {
        private String groupName;
        private List<Attr> attrs;//规格参数
    }

}
