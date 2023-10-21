package com.zzclearning.gulimall.coupon.dao;

import com.zzclearning.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀活动商品关联
 * 
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 21:58:04
 */
@Mapper
public interface SeckillSkuRelationDao extends BaseMapper<SeckillSkuRelationEntity> {
	
}
