package com.zzclearning.gulimall.coupon.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.zzclearning.to.SkuReductionTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zzclearning.gulimall.coupon.entity.SkuLadderEntity;
import com.zzclearning.gulimall.coupon.service.SkuLadderService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.R;



/**
 * 商品阶梯价格
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 21:58:04
 */
@RestController
@RequestMapping("coupon/skuladder")
public class SkuLadderController {
    @Autowired
    private SkuLadderService skuLadderService;

    /**
     * 列表
     */
    @RequestMapping("/list")
        public R list(@RequestParam Map<String, Object> params){
        PageUtils page = skuLadderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("coupon:skuladder:info")
    public R info(@PathVariable("id") Long id){
		SkuLadderEntity skuLadder = skuLadderService.getById(id);

        return R.ok().put("skuLadder", skuLadder);
    }

    /**
     * 保存
     */
    //@RequestMapping("/save")
    ////@RequiresPermissions("coupon:skuladder:save")
    //public R save(@RequestBody SkuLadderEntity skuLadder){
	//	skuLadderService.save(skuLadder);
    //
    //    return R.ok();
    //}

    /**
     * 保存折扣、满减、会员价格
     * @param skuReductionTo
     * @return
     */
    @PostMapping("/save")
    public R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo) {
        Boolean result = skuLadderService.saveSkuReduction(skuReductionTo);
        return R.ok();
    };

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("coupon:skuladder:update")
    public R update(@RequestBody SkuLadderEntity skuLadder){
		skuLadderService.updateById(skuLadder);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("coupon:skuladder:delete")
    public R delete(@RequestBody Long[] ids){
		skuLadderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
