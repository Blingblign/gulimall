package com.zzclearning.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.gulimall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 21:58:04
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

