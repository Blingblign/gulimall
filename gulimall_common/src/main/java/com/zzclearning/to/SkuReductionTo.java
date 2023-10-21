package com.zzclearning.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 满减、折扣、会员价格信息
 * @author bling
 * @create 2022-11-02 15:57
 */
@Data
public class SkuReductionTo {
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * 满几件
     */
    private Integer fullCount;
    /**
     * 打几折
     */
    private BigDecimal discount;
    /**
     * 是否叠加其他打折优惠
     */
    private int countStatus;
    /**
     * 满多少
     */
    private BigDecimal fullPrice;
    /**
     * 减多少
     */
    private BigDecimal reducePrice;
    /**
     * 是否叠加其他满减优惠
     */
    private int priceStatus;
    ///**
    // * 会员价格
    // */
    //List<MemberPrice> memberPrices;

}
