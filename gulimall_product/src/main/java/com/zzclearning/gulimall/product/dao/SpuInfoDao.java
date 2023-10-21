package com.zzclearning.gulimall.product.dao;

import com.zzclearning.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * spu信息
 * 
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 16:44:33
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    List<Long> getSearchIds(@Param("allIds") List<Long> allIds);
}
