package com.zzclearning.gulimall.product.vo;

import lombok.Data;

/**
 * @author bling
 * @create 2023-02-20 9:37
 */
@Data
public class OrderItemTo {
    /**
     * 商品sku编号
     */
    private Long skuId;
    /**
     * 商品sku名字
     */
    private String skuName;
    /**
     * spu_id
     */
    private Long spuId;
    /**
     * spu_name
     */
    private String spuName;
    /**
     * spu_pic
     */
    private String spuPic;
    /**
     * 品牌
     */
    private String spuBrand;
    /**
     * 商品分类id
     */
    private Long categoryId;

}
