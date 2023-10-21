package com.zzclearning.vo;

import lombok.Data;

import java.util.List;

/**
 * @author bling
 * @create 2023-02-20 14:44
 */
@Data
public class SkuLockStockVo {
    private String                 orderSn;
    private List<SkuOrderLockItem> items;
    private String consignee;
    private String consigneeTel;
    private String deliveryAddress;

    @Data
    public static class SkuOrderLockItem {
        private Long skuId;
        private String skuName;
        private Integer skuNum;
    }
}
