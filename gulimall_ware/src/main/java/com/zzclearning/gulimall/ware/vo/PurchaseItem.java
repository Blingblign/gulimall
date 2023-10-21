package com.zzclearning.gulimall.ware.vo;

import lombok.Data;

/**
 * 采购单需求项详情
 * @author bling
 * @create 2022-11-03 14:05
 */
@Data
public class PurchaseItem {
    private Long    itemId;//采购需求项id
    private Integer status;//采购需求项状态
    private String  reason;//失败原因
}
