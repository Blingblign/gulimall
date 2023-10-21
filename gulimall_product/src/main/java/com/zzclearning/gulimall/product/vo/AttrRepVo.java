package com.zzclearning.gulimall.product.vo;

import lombok.Data;

/**
 *
 * @author bling
 * @create 2022-10-28 23:44
 */
@Data
public class AttrRepVo extends AttrVo{
    /**
     * 所属分类名"手机/数码/手机"
     */
    private String catelogName;
    /**
     * 所属分组名
     */
    private String groupName;
}
