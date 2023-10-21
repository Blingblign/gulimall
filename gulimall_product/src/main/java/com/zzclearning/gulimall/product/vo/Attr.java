package com.zzclearning.gulimall.product.vo;

import lombok.Data;

/**
 * 添加商品功能：销售属性vo
 * 规格参数vo
 * @author bling
 * @create 2022-11-02 12:06
 */
@Data
public class Attr {
    private Long attrId;
    private String attrName;
    private String attrValue;
}
