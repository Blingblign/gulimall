package com.zzclearning.gulimall.product.dao;

import com.zzclearning.gulimall.product.entity.SkuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku信息
 * 
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 16:44:33
 */
@Mapper
public interface SkuInfoDao extends BaseMapper<SkuInfoEntity> {


}
