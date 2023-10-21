package com.zzclearning.gulimall.product.vo;

import lombok.Data;

/**
 * 添加商品功能：基本属性vo
 * @author bling
 * @create 2022-11-02 12:06
 */
@Data
public class BaseAttrs {
    private Long attrId;
    private String attrValues;
    private int showDesc;
}