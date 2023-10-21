package com.zzclearning.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.gulimall.ware.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 22:03:50
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

