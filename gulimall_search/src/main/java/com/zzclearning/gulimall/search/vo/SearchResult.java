package com.zzclearning.gulimall.search.vo;

import com.zzclearning.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

/**
 * @author bling
 * @create 2023-02-06 21:11
 */
@Data
public class SearchResult {
    private List<SkuEsModel> products;//检索得到的商品信息
    private List<BrandVo> brands;
    private List<CatalogVo> catalogs;
    private List<AttrVo> attrs;
    private List<Long> attrIds;//其他检索到的属性
    //分页信息
    private Integer pageNum;//当前页
    private Long totalRecords;//总记录数
    private Long totalPage;//总页码
    private List<Integer> pageNavs;//导航页

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImage;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValues;
    }

}
