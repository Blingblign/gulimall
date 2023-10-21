package com.zzclearning.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.gulimall.coupon.entity.SkuLadderEntity;
import com.zzclearning.to.SkuReductionTo;

import java.util.Map;

/**
 * 商品阶梯价格
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 21:58:04
 */
public interface SkuLadderService extends IService<SkuLadderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    Boolean saveSkuReduction(SkuReductionTo skuReductionTo);
}

