package com.zzclearning.gulimall.ware.dao;

import com.zzclearning.gulimall.ware.entity.PurchaseEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购信息
 * 
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 22:03:50
 */
@Mapper
public interface PurchaseDao extends BaseMapper<PurchaseEntity> {
	
}
