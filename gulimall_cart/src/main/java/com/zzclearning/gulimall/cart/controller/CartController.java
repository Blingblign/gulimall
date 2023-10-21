package com.zzclearning.gulimall.cart.controller;

import com.zzclearning.common.utils.R;
import com.zzclearning.gulimall.cart.service.CartService;
import com.zzclearning.gulimall.cart.vo.Cart;
import com.zzclearning.gulimall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 购物车分为临时/用户购物车两种：
 * 未登录时，使用临时购物车，通过cookie ：user-key=xxx标识
 * 用户登录后，将临时购物车数据与用户购物车数据合并
 *
 * 购物车数据：都存储在redis中，键的前缀为gulimall:cart:，后面接user-key或者用户id；
 * Map<String,Map<String,String>>,用hashMap，以skuId为键，cartItem为值
 *
 * 拦截器：
 * 用于判断用户是否登录，userId；
 * 给每个临时/非临时用户分配一个user-key
 *通过threadlocal为当前线程绑定用户数据userInfo
 *
 * @author bling
 * @create 2023-02-14 9:52
 */
@Controller
public class CartController {
    @Autowired
    CartService cartService;
    /**
     * 添加商品到购物车
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/addCartItem")
    public String addCartItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        cartService.addCartItem(skuId,num);
        redirectAttributes.addAttribute("skuId",skuId);//会以skuId=xx添加到url后面
        return "redirect:http://cart.gulimall.com/success";
    }

    /**
     * 跳转到添加购物车成功界面
     * @return
     */
    @GetMapping("/success")
    public String addCartItemsuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        CartItem item = cartService.getCartItem(skuId);
        model.addAttribute("cartItem",item);
        return "success";
    }

    /**
     * 获取购物车列表
     */
    @GetMapping("/cart.html")
    public String getCartList(Model model) {
        Cart cart = cartService.getCartList();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    /**
     * 删除购物车中的商品
     */
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 改变选中状态
     * @param skuId
     * @param check
     * @return
     */
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("checked") Integer check) {
        cartService.checkItem(skuId,check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 改变商品数量
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        cartService.countItem(skuId,num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 订单服务调用，并且更新商品价格
     * 查询用户选中的购物车列表
     */
    @ResponseBody
    @GetMapping("/checkedItems")
    public R getCheckedCartItemList() {
        List<CartItem> cartItemList = cartService.getCheckedCartItemList();
        return R.ok().setData(cartItemList);
    }

}
