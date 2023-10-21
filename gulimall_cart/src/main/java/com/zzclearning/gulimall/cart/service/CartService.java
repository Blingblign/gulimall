package com.zzclearning.gulimall.cart.service;

import com.zzclearning.gulimall.cart.vo.Cart;
import com.zzclearning.gulimall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author bling
 * @create 2023-02-14 10:46
 */
public interface CartService {
    void addCartItem(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);

    Cart getCartList();

    void deleteItem(Long skuId);

    void checkItem(Long skuId, Integer check);

    void countItem(Long skuId, Integer num);

    List<CartItem> getCheckedCartItemList();
}
