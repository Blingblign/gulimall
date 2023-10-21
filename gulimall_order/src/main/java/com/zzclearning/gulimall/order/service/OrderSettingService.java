package com.zzclearning.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.gulimall.order.entity.OrderSettingEntity;

import java.util.Map;

/**
 * 订单配置信息
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 22:05:25
 */
public interface OrderSettingService extends IService<OrderSettingEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

