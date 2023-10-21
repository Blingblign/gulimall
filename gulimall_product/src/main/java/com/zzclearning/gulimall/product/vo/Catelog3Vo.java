package com.zzclearning.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * "catalog2Id": "二级分类ID",
 *      "id": "三级分类ID",
 *      "name": "三级分类名"
 * @author bling
 * @create 2023-01-31 11:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catelog3Vo {
    private String catelog2Id;
    private String id;
    private String name;
}
