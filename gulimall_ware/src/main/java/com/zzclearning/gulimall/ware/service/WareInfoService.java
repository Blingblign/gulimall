package com.zzclearning.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.gulimall.ware.entity.WareInfoEntity;
import com.zzclearning.vo.AddressFare;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 22:03:50
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    AddressFare calculateFare(Long addrId);
}

