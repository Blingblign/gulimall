package com.zzclearning.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author bling
 * @create 2022-11-03 13:19
 */
@Data
public class DonePurchaseVo {
    private Long               id; // 123,采购单id
    private List<PurchaseItem> items;//完成，失败的需求详情
}
