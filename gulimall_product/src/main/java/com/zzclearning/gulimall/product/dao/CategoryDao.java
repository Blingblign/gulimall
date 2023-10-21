package com.zzclearning.gulimall.product.dao;

import com.zzclearning.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品三级分类
 * 
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 16:44:33
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {

    List<CategoryEntity> getLevel2Categories(@Param("cat1Id") Long cat1Id);

    List<CategoryEntity> getLevel3Categories(@Param("cat2Id") Long cat2Id);
}
