package com.zzclearning.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * keyword=小米&sort=saleCount_desc/asc&hasStock=0/1&skuPrice=400_1900&brandId=1&catalog3Id=1&attrs=1_3G:4G:5G&attrs=2_骁龙845&attrs=4_高清屏
 * @author bling
 * @create 2023-02-06 21:03
 */
@Data
public class SearchParamsVo {
    private String       keyword;
    private String       sort;
    private Integer      hasStock;
    private String skuPrice;
    private List<Long> brandId;
    private Long         catalog3Id;
    private List<String> attrs;
    private Integer pageNum = 1;
    //private Integer pageSize = 2;
}
