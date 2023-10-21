package com.zzclearning.gulimall.coupon.dao;

import com.zzclearning.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 21:58:04
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
