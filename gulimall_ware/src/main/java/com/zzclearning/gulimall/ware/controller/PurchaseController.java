package com.zzclearning.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.zzclearning.gulimall.ware.vo.DonePurchaseVo;
import com.zzclearning.gulimall.ware.vo.MergeDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zzclearning.gulimall.ware.entity.PurchaseEntity;
import com.zzclearning.gulimall.ware.service.PurchaseService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.R;



/**
 * 采购信息
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 22:03:50
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 列表
     */
    @GetMapping("/list")
        public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @GetMapping("/info/{id}")
    //@RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    //@RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
        purchase.setStatus(0);
        purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    //@RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    //@RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }
    /**
     * 获取未被领取的采购单
     */
    @GetMapping("/unreceive/list")
    public R getUnreceivedPurchase() {
        PageUtils page = purchaseService.getUnreceivedPurchase();
        return R.ok().put("page",page);
    }


    /**
     * 合并采购需求
     */
    @PostMapping("/merge")
    public R mergePurchaseDetails(@RequestBody MergeDetailVo mergeDetailVo) {
        Boolean result = purchaseService.mergePurchaseDetails(mergeDetailVo);
        return R.ok();
    }

    /**
     * 领取采购单
     */
    @PostMapping("/received")
    public R receivePurchases(@RequestBody List<Long> ids) {
        Boolean result = purchaseService.receivePurchases(ids);
        return R.ok();
    }

    /**
     * 完成采购
     */
    @PostMapping("/done")
    public R purchaseDone(@RequestBody DonePurchaseVo doneVo) {
        Boolean result = purchaseService.purchaseDone(doneVo);
        return R.ok();
    }



}