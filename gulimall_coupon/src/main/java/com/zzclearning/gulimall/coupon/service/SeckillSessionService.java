package com.zzclearning.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.gulimall.coupon.entity.SeckillSessionEntity;
import com.zzclearning.to.seckill.SeckillSessionWithSkusTo;

import java.util.List;
import java.util.Map;

/**
 * 秒杀活动场次
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 21:58:04
 */
public interface SeckillSessionService extends IService<SeckillSessionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SeckillSessionWithSkusTo> getRecentThreeDaysSessionsWithSkus();
}

