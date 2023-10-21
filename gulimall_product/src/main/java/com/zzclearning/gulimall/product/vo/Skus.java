package com.zzclearning.gulimall.product.vo;

import com.zzclearning.to.MemberPrice;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 添加商品功能：sku信息
 * @author bling
 * @create 2022-11-02 12:04
 */
@Data
public class Skus {
    private    List<Attr>    attr;
    private    String    skuName;
    private BigDecimal price;
    private    String    skuTitle;
    private    String    skuSubtitle;
    private    List<Images>    images;
    private List<String> descar;
    private    int    fullCount;
    private    BigDecimal    discount;
    private    int    countStatus;
    private    BigDecimal    fullPrice;
    private    BigDecimal    reducePrice;
    private    int               priceStatus;
    private    List<MemberPrice> memberPrice;
}

