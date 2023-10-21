package com.zzclearning.gulimall.coupon.service.impl;

import com.zzclearning.gulimall.coupon.entity.MemberPriceEntity;
import com.zzclearning.gulimall.coupon.entity.SkuFullReductionEntity;
import com.zzclearning.gulimall.coupon.service.MemberPriceService;
import com.zzclearning.gulimall.coupon.service.SkuFullReductionService;
import com.zzclearning.to.SkuReductionTo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.peer.MenuPeer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.Query;

import com.zzclearning.gulimall.coupon.dao.SkuLadderDao;
import com.zzclearning.gulimall.coupon.entity.SkuLadderEntity;
import com.zzclearning.gulimall.coupon.service.SkuLadderService;


@Service("skuLadderService")
public class SkuLadderServiceImpl extends ServiceImpl<SkuLadderDao, SkuLadderEntity> implements SkuLadderService {
    @Autowired
    SkuFullReductionService skuFullReductionService;
    @Autowired
    MemberPriceService memberPriceService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuLadderEntity> page = this.page(
                new Query<SkuLadderEntity>().getPage(params),
                new QueryWrapper<SkuLadderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public Boolean saveSkuReduction(SkuReductionTo skuReductionTo) {
        //满几件打几折
        if (skuReductionTo.getFullCount() > 0) {
            SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
            BeanUtils.copyProperties(skuReductionTo,skuLadderEntity);
            skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
            this.save(skuLadderEntity);
        }
        //满多少减多少
        if (skuReductionTo.getFullPrice().compareTo(new BigDecimal(0)) > 0) {
            SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
            BeanUtils.copyProperties(skuReductionTo,skuFullReductionEntity);
            skuFullReductionEntity.setAddOther(skuReductionTo.getPriceStatus());
            skuFullReductionService.save(skuFullReductionEntity);
        }
        return true;
    }

}