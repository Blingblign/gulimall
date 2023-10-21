package com.zzclearning.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.gulimall.product.entity.SpuInfoEntity;
import com.zzclearning.gulimall.product.vo.SpuInfoVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 16:44:33
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 按条件检索spu列表
     * @param params
     * @return
     */
    PageUtils queryByConditions(Map<String, Object> params);

    /**
     * 保存商品发布信息
     * @param spuInfoVo
     * @return
     */
    Boolean saveSpuInfo(SpuInfoVo spuInfoVo);

    void productUp(Long spuId);
}

