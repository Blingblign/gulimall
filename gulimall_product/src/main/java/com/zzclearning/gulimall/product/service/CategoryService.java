package com.zzclearning.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.gulimall.product.entity.CategoryEntity;
import com.zzclearning.gulimall.product.vo.Catelog2Vo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 16:44:33
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取商品三级分类菜单
     * @return
     */
    List<CategoryEntity> getTreeMenus();

    Long[] getCategoryPath(Long catelogId);

    /**
     * 组装三级分类名 "手机/数码/手机"
     * @param catelogId
     * @return
     */
    String getCategoryName(Long catelogId);

    List<CategoryEntity> getLevel1Categories();

    List<CategoryEntity> getLevel2Categories(Long cat1Id);

    List<CategoryEntity> getLevel3Categories(Long cat2Id);

    Map<String, List<Catelog2Vo>> getCatelogJson();
}

