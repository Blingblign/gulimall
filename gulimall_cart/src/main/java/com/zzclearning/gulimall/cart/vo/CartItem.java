package com.zzclearning.gulimall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物项
 * @author bling
 * @create 2023-02-14 9:52
 */
public class CartItem {
    private Long         skuId;
    private Boolean      check = true;//是否被选中
    private String       image;//商品图片
    private String       title;//商品名称
    private List<String> skuAttrValues;//商品销售属性
    private BigDecimal   price;//商品单价
    private Integer      count;//商品数量
    private BigDecimal   totalPrice;//商品总价

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getSkuAttrValues() {
        return skuAttrValues;
    }

    public void setSkuAttrValues(List<String> skuAttrValues) {
        this.skuAttrValues = skuAttrValues;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getTotalPrice() {
        totalPrice = this.price.multiply(BigDecimal.valueOf(this.count));//动态计算总价
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
