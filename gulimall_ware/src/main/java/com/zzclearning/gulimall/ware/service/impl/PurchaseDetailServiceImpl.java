package com.zzclearning.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.nio.channels.WritePendingException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.Query;

import com.zzclearning.gulimall.ware.dao.PurchaseDetailDao;
import com.zzclearning.gulimall.ware.entity.PurchaseDetailEntity;
import com.zzclearning.gulimall.ware.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseDetailEntity> wrapper = Wrappers.lambdaQuery(PurchaseDetailEntity.class);
        String key = (String) params.get("key");
        String status = (String) params.get("status");
        String wareId = (String) params.get("wareId");
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(w->{
                w.eq(PurchaseDetailEntity::getSkuId,key).or().eq(PurchaseDetailEntity::getPurchaseId,key);
            });
        }
        wrapper.eq(StringUtils.isNotBlank(status),PurchaseDetailEntity::getStatus,status)
                .eq(StringUtils.isNotBlank(wareId),PurchaseDetailEntity::getWareId,wareId);
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}