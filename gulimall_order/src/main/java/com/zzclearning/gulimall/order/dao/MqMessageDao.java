package com.zzclearning.gulimall.order.dao;

import com.zzclearning.gulimall.order.entity.MqMessageEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * 
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 22:05:25
 */
@Mapper
public interface MqMessageDao extends BaseMapper<MqMessageEntity> {
	
}
