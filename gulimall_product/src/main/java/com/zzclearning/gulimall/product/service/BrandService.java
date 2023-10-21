package com.zzclearning.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.gulimall.product.entity.BrandEntity;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

/**
 * 品牌
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 16:44:33
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    boolean batchSave(List<BrandEntity> brands, @Nullable Long catalogId);
}

