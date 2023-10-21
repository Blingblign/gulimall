package com.zzclearning.gulimall.product.dao;

import com.zzclearning.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 16:44:33
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    int removeBatch(@Param("entities") List<AttrAttrgroupRelationEntity> entities);

}
