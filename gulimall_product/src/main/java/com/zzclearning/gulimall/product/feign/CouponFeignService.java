package com.zzclearning.gulimall.product.feign;

import com.zzclearning.common.utils.R;
import com.zzclearning.to.MemberPrice;
import com.zzclearning.to.SkuReductionTo;
import com.zzclearning.to.SpuBoundsTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * @author bling
 * @create 2022-11-02 15:03
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    /**
     * 保存
     * feign远程调用，@RequestBody注解先将对象转为json格式放在请求体中，再给/coupon/spubounds/save发送请求
     * 对方服务gulimall-coupon接收到请求，请求体中有json数据，通过@RequestBody注解将json数据转换为对应的实体对象
     * （只要json数据模型是兼容的，双方服务无需使用同一个To）
     */
    @RequestMapping("/coupon/spubounds/save")
    //@RequiresPermissions("coupon:spubounds:save")
    R saveBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    @PostMapping("/coupon/skuladder/save")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);

    @PostMapping("/coupon/memberprice/save")
    //@RequiresPermissions("coupon:memberprice:save")
    public R saveMemberPrices(@RequestBody List<MemberPrice> memberPrices);
}
