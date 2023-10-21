package com.zzclearning.gulimall.ware.vo;

import lombok.Data;

/**
 * 锁定商品库存
 * @author bling
 * @create 2023-02-20 11:06
 */
@Data
public class StockLockVo {
    private Long skuId;
    private Integer count;
}
