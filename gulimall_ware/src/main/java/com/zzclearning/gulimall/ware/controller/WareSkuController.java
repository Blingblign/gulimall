package com.zzclearning.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.zzclearning.common.exception.BizExceptionEnum;
import com.zzclearning.gulimall.ware.exception.StockNotEnoughException;
import com.zzclearning.gulimall.ware.vo.StockLockVo;
import com.zzclearning.to.SkuHaStockVo;
import com.zzclearning.vo.SkuLockStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zzclearning.gulimall.ware.entity.WareSkuEntity;
import com.zzclearning.gulimall.ware.service.WareSkuService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.R;



/**
 * 商品库存
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 22:03:50
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 列表
     */
    @RequestMapping("/list")
        public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }
    /**
     * 查询库存(多仓库库存汇总查询)
     */
    @PostMapping("/hasStock")
    public R skusHasStock(@RequestBody List<Long> skuIds) {
        List<SkuHaStockVo> skuHaStockVos = wareSkuService.getStock(skuIds);
        return R.ok().put("data",skuHaStockVos);
    }

    /**
     * 锁库存
     */
    @PostMapping("/lockStock")
    public R lockStock(@RequestBody SkuLockStockVo skuLockStockVo) {
        try {
            wareSkuService.orderLockStock(skuLockStockVo);
        } catch (StockNotEnoughException e) {
            return R.error(BizExceptionEnum.NO_STOCK_EXCEPTION.getCode(), BizExceptionEnum.NO_STOCK_EXCEPTION.getMessage());
        }
        return R.ok();
    }

}
