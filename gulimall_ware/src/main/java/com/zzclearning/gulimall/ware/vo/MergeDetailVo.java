package com.zzclearning.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author bling
 * @create 2022-11-03 10:07
 */
@Data
public class MergeDetailVo {
    private Long       purchaseId;//整单id
    private List<Long> items;//[1,2,3,4] 合并项集合
}
