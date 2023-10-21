package com.zzclearning.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * "catalog1Id": "一级分类ID",
 *      "id": "二级分类ID",
 *      "name": "二级分类名",
 *      "catalog3List":
 * @author bling
 * @create 2023-01-31 15:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catelog2Vo {

    private String id;
    private String catelog1Id;
    private String name;
    private List<Catelog3Vo> catelog3Vos;
}
