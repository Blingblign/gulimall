package com.zzclearning.gulimall.member.dao;

import com.zzclearning.gulimall.member.entity.MemberLoginLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员登录记录
 * 
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 22:07:05
 */
@Mapper
public interface MemberLoginLogDao extends BaseMapper<MemberLoginLogEntity> {
	
}
