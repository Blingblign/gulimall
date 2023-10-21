package com.zzclearning.gulimall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车
 * @author bling
 * @create 2023-02-14 9:52
 */
public class Cart {
    private List<CartItem> items;
    private Integer        totalCount;//商品总数
    private Integer    cartNum;//商品样数
    private BigDecimal totalAmount = new BigDecimal(0);//所有商品总价格
    private BigDecimal reduce = new BigDecimal(0);//优惠价

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getTotalCount() {
        for (CartItem cartItem : items) {
            totalCount += cartItem.getCount();
        }
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getCartNum() {
        cartNum =  items.size();
        return cartNum;
    }

    public void setCartNum(Integer cartNum) {
        this.cartNum = cartNum;
    }

    public BigDecimal getTotalAmount() {
        if (items != null && items.size() > 0) {
            for (CartItem cartItem : items) {
                if (cartItem.getCheck()) {
                    totalAmount = totalAmount.add(cartItem.getTotalPrice());

                }
            }
        }
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
