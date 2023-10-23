package com.zzclearning.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.zzclearning.common.utils.R;
import com.zzclearning.gulimall.cart.constant.CartConstant;
import com.zzclearning.gulimall.cart.feign.ProductFeignService;
import com.zzclearning.gulimall.cart.interceptor.CartInterceptor;
import com.zzclearning.gulimall.cart.service.CartService;
import com.zzclearning.gulimall.cart.to.SkuInfoTo;
import com.zzclearning.gulimall.cart.vo.Cart;
import com.zzclearning.gulimall.cart.vo.CartItem;
import com.zzclearning.gulimall.cart.vo.UserInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author bling
 * @create 2023-02-14 10:46
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public void addCartItem(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        //先查看redis中是否有该商品数据
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        String cartItemString = (String)ops.get(skuId.toString());
        if (!StringUtils.isEmpty(cartItemString)) {
            //redis中有商品数据
            CartItem cartItem = JSON.parseObject(cartItemString, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);//更新商品数量
            ops.put(skuId.toString(),JSON.toJSONString(cartItem));
            return;
        }
        //添加新商品到购物车
        CartItem cartItem = new CartItem();
        //远程查询sku信息 TODO 线程池异步
        CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(() -> {
            R result = productFeignService.info(skuId);
            if (result.getCode() == 0) {
                SkuInfoTo skuInfo = result.getData("data", new TypeReference<SkuInfoTo>() {
                });
                cartItem.setSkuId(skuId);
                cartItem.setCount(num);
                cartItem.setImage(skuInfo.getSkuDefaultImg());
                cartItem.setTitle(skuInfo.getSkuTitle());
                cartItem.setPrice(skuInfo.getPrice());
            }
        }, executor);
        CompletableFuture<Void> skuAttrFuture = CompletableFuture.runAsync(() -> {
            //远程查询销售属性信息
            List<String> skuAttrvalues = productFeignService.getSkuAttrvalues(skuId);
            cartItem.setSkuAttrValues(skuAttrvalues);
        }, executor);
        CompletableFuture.allOf(skuInfoFuture,skuAttrFuture).get();
        ops.put(skuId.toString(),JSON.toJSONString(cartItem));
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        String s = (String) ops.get(skuId.toString());
        return JSON.parseObject(s, CartItem.class);
    }

    @Override
    public Cart getCartList() {
        UserInfoVo userInfoVo = CartInterceptor.threadLocal.get();
        Cart cart = new Cart();
        //临时用户，直接返回临时购物车数据
        if (userInfoVo.getUserId() == null) {
            String userKey = CartConstant.CART_PREFIX + userInfoVo.getUserKey();
            List<CartItem> cartItems = getCartItems(userKey);
            cart.setItems(cartItems);
        } else {
            //合并购物车数据
            List<CartItem> cartItems = mergeCart(userInfoVo);
            cart.setItems(cartItems);
        }
        return cart;

    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String s = (String) cartOps.get(skuId.toString());
        if (!StringUtils.isEmpty(s)) {
            CartItem cartItem = JSON.parseObject(s, CartItem.class);
            cartItem.setCheck(check != 0 );
            cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));
        }

    }

    @Override
    public void countItem(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String s = (String) cartOps.get(skuId.toString());
        if (!StringUtils.isEmpty(s)) {
            CartItem cartItem = JSON.parseObject(s, CartItem.class);
            cartItem.setCount(num);
            cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));
        }
    }

    @Override
    public List<CartItem> getCheckedCartItemList() {
        UserInfoVo userInfoVo = CartInterceptor.threadLocal.get();
        //用户未登录，返回null
        if (userInfoVo.getUserId() == null) return null;
        //获得已选中的购物项，并以最新价格返回
        String cartKey = CartConstant.CART_PREFIX + userInfoVo.getUserId();
        List<CartItem> cartItems = getCartItems(cartKey);
        if (cartItems != null && cartItems.size()>0) {
            //for (CartItem cartItem : cartItems) {
            //    if (!cartItem.getCheck()) {
            //        cartItems.remove(cartItem);//在循环迭代时不能修改集合元素，ConcurrentModificationException
            //    } else {
            //        //更新价格
            //        R res = productFeignService.info(cartItem.getSkuId());
            //        SkuInfoTo skuInfo = res.getData("skuInfo", new TypeReference<SkuInfoTo>() {
            //        });
            //        cartItem.setPrice(skuInfo.getPrice());
            //    }
            //
            //}
            cartItems = cartItems.stream().filter(CartItem::getCheck).peek(cartItem -> {
                        //更新价格
                        R res = productFeignService.info(cartItem.getSkuId());
                        SkuInfoTo skuInfo = res.getData("data", new TypeReference<SkuInfoTo>() {
                        });
                        cartItem.setPrice(skuInfo.getPrice());
            }).collect(Collectors.toList());
        }

        return cartItems;

    }

    /**
     * 合并购物车数据
     */
    private List<CartItem> mergeCart(UserInfoVo userInfoVo) {
        String tempCartKey = CartConstant.CART_PREFIX + userInfoVo.getUserKey();
        String cartKey = CartConstant.CART_PREFIX + userInfoVo.getUserId();
        Map<String,String> tempCartMap = getCartMap(tempCartKey);
        Map<String,String> cartMap = getCartMap(cartKey);
        //临时购物车为空
        if (tempCartMap == null) {
            if (cartMap == null) {
                return null;
            }
            return cartMap.values().stream().map(s -> JSON.parseObject((String) s, CartItem.class)).collect(Collectors.toList());
        } else {
            if (cartMap == null) {
                //合并并删除数据
                redisTemplate.boundHashOps(cartKey).putAll(tempCartMap);
                redisTemplate.delete(tempCartKey);
                return tempCartMap.values().stream().map(s -> JSON.parseObject(s, CartItem.class)).collect(Collectors.toList());
            }
            //两者都不为空
            Set<String> skuIds = cartMap.keySet();
            for (String skuId : skuIds) {
                if (tempCartMap.containsKey(skuId)) {
                    String s = tempCartMap.get(skuId);
                    CartItem cartItem = JSON.parseObject(s, CartItem.class);
                    cartItem.setCount(cartItem.getCount() + JSON.parseObject(cartMap.get(skuId),CartItem.class).getCount());
                    tempCartMap.put(skuId,JSON.toJSONString(cartItem));
                }
            }
            redisTemplate.boundHashOps(cartKey).putAll(tempCartMap);
            redisTemplate.delete(tempCartKey);
            return getCartItems(cartKey);
        }


    }
    private Map<String,String> getCartMap(String cartKey) {
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(cartKey);
        return ops.entries();
    }

    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(cartKey);
        List<Object> values = ops.values();
        if (values != null && values.size() > 0) {
            List<CartItem> cartItems = values.stream().map(value -> {
                String cartItemString = (String) value;
                return JSON.parseObject(cartItemString, CartItem.class);
            }).collect(Collectors.toList());
            return cartItems;
        }
        return null;
    }

    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoVo userInfoVo = CartInterceptor.threadLocal.get();
        String cartKey;
        if (userInfoVo.getUserId() != null) {
            //用户已登录
            cartKey = CartConstant.CART_PREFIX + userInfoVo.getUserId();
        } else {
            //临时用户
            cartKey = CartConstant.CART_PREFIX + userInfoVo.getUserKey();
        }
        return redisTemplate.boundHashOps(cartKey);
    }
}
