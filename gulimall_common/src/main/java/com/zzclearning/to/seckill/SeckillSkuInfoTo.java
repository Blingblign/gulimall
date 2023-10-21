package com.zzclearning.to.seckill;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 秒杀商品to
 * @author bling
 * @create 2023-02-25 15:56
 */
@Data
public class SeckillSkuInfoTo {
    /**
     * 活动id
     */
    private Long      promotionId;
    /**
     * 活动场次id
     */
    private Long       promotionSessionId;
    /**
     * 商品id
     */
    private Long       skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private Integer    seckillCount;
    /**
     * 每人限购数量
     */
    private Integer seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;
    /**
     * 商品详细信息
     */
    private SkuInfoTo skuInfo;
    /**
     * 每日开始时间
     */
    private Date      startTime;
    /**
     * 每日结束时间
     */
    private Date      endTime;
    /**
     * 商品秒杀随机码
     */
    private String randomCode;

    /**
     * 是否正在参与秒杀
     */
    private Boolean isKill = false;
}
