package com.zzclearning.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.gulimall.product.entity.AttrGroupEntity;
import com.zzclearning.gulimall.product.vo.AttrGroupVo;
import com.zzclearning.gulimall.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 16:44:33
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long categoryId);

    List<AttrGroupVo> getGroupsWithAttrs(Long catelogId);

    List<SkuItemVo.AttrGroupWithAttrsVo> getAttrGroupWithAttrs(Long spuId);

    void batchSave(List<AttrGroupVo> attrGroupVos);

}

