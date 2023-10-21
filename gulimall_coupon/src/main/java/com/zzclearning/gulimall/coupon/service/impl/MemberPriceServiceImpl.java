package com.zzclearning.gulimall.coupon.service.impl;

import com.zzclearning.to.MemberPrice;
import org.springframework.stereotype.Service;

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

import com.zzclearning.gulimall.coupon.dao.MemberPriceDao;
import com.zzclearning.gulimall.coupon.entity.MemberPriceEntity;
import com.zzclearning.gulimall.coupon.service.MemberPriceService;
import org.springframework.web.bind.annotation.RequestBody;


@Service("memberPriceService")
public class MemberPriceServiceImpl extends ServiceImpl<MemberPriceDao, MemberPriceEntity> implements MemberPriceService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberPriceEntity> page = this.page(
                new Query<MemberPriceEntity>().getPage(params),
                new QueryWrapper<MemberPriceEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public Boolean saveMemberPrices( List<MemberPrice> memberPrices) {
        //会员价格
        List <MemberPriceEntity> memberPriceEntities = memberPrices.stream().map(ms -> {
            MemberPriceEntity memberPriceEntity = null;
            if (!Objects.equals(ms.getPrice(), new BigDecimal(0))) {
                memberPriceEntity = new MemberPriceEntity();
                memberPriceEntity.setSkuId(ms.getSkuId());
                memberPriceEntity.setMemberLevelId(ms.getId());
                memberPriceEntity.setMemberLevelName(ms.getName());
                memberPriceEntity.setMemberPrice(ms.getPrice());
            }
            return memberPriceEntity;
        }).collect(Collectors.toList());
        this.saveBatch(memberPriceEntities);
        return true;
    }

}