package com.zzclearning.gulimall.product.vo;

import com.zzclearning.to.MemberPrice;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 添加商品功能：spu信息
 * @author bling
 * @create 2022-11-02 12:03
 */
@Data
public class SpuInfoVo {
    private String           spuName;
    private String           spuDescription;
    private Long             catalogId;
    private Long             brandId;
    private BigDecimal       weight;
    private int              publishStatus;
    private List <String>    decript;
    private List <String>    images;
    private Bounds           bounds;
    private List <BaseAttrs> baseAttrs;
    private List <Skus>      skus;
}


