package com.zzclearning.gulimall.seckill.controller;

import com.zzclearning.common.utils.R;
import com.zzclearning.gulimall.seckill.service.SeckillService;
import com.zzclearning.to.seckill.SeckillSkuInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author bling
 * @create 2023-02-25 15:50
 */
@RestController
public class SeckillController {
    @Autowired
    SeckillService seckillService;
    /**
     * 获取当前时间对应场次秒杀信息
     * @return
     */
    @GetMapping("/getCurrentSeckillSkus")
    public R getCurrentSeckillSkus() {
        List<SeckillSkuInfoTo> skus = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(skus);
    }

    /**
     * 查看当前商品是否参与秒杀/秒杀预告
     * @return
     */
    @GetMapping("/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId) {
        SeckillSkuInfoTo seckillSkuInfoTo = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(seckillSkuInfoTo);
    }

    /**
     * 秒杀商品
     * @param
     * @return
     */
    @GetMapping("/kill")
    public R killSku(@RequestParam("killId") String killId, @RequestParam("key") String key, @RequestParam("num") Integer num) {
        seckillService.killSku(killId, key, num);
        return R.ok();
    }

}
