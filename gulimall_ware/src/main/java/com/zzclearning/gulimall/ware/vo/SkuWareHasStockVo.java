package com.zzclearning.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * 商品在哪些仓库有库存
 * @author bling
 * @create 2023-02-20 14:50
 */
@Data
public class SkuWareHasStockVo {
    private Long skuId;
    private List<Long> wareIds;
}
