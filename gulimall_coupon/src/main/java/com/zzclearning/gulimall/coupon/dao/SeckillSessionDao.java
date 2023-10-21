package com.zzclearning.gulimall.coupon.dao;

import com.zzclearning.gulimall.coupon.entity.SeckillSessionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 秒杀活动场次
 * 
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 21:58:04
 */
@Mapper
public interface SeckillSessionDao extends BaseMapper<SeckillSessionEntity> {

    List<SeckillSessionEntity> getRecentThreeDaysSessionsWithSkus(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
