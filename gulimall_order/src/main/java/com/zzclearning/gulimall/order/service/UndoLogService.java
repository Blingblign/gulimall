package com.zzclearning.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.gulimall.order.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 22:05:25
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

