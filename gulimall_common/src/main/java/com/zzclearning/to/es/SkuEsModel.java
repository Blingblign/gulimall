package com.zzclearning.to.es;


import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bling
 * @create 2023-02-06 11:18
 */
@Data
public class SkuEsModel {
    private Long       skuId;
    private Long       spuId;
    private String     skuTitle;
    private String     skuImage;
    private BigDecimal skuPrice;
    private Long       saleCount;
    private Boolean    hasStock;
    private Long hotScore;
    private Long brandId;
    private String brandName;
    private String brandImage;
    private Long catalogId;
    private String catalogName;
    private List<Attrs> attrs;
    private List<String> suggestion = new ArrayList<>();//自动补全字段

    @Data
    public static class Attrs {
        private Long attrId;
        private String attrName;
        private String attrValue;
    }
}
