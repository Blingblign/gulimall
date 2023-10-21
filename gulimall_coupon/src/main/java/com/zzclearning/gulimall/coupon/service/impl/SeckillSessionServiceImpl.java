package com.zzclearning.gulimall.coupon.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzclearning.common.utils.R;
import com.zzclearning.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.zzclearning.gulimall.coupon.feign.ProductFeignService;
import com.zzclearning.gulimall.coupon.service.SeckillSkuRelationService;
import com.zzclearning.to.seckill.SeckillSessionWithSkusTo;
import com.zzclearning.to.seckill.SeckillSkuInfoTo;
import com.zzclearning.to.seckill.SkuInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.Query;

import com.zzclearning.gulimall.coupon.dao.SeckillSessionDao;
import com.zzclearning.gulimall.coupon.entity.SeckillSessionEntity;
import com.zzclearning.gulimall.coupon.service.SeckillSessionService;

@Slf4j
@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {
    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;
    @Autowired
    ProductFeignService productFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionWithSkusTo> getRecentThreeDaysSessionsWithSkus() {
        String startTime = getTime(LocalDate.now(), LocalTime.MIN);
        String endTime = getTime(LocalDate.now().plusDays(2), LocalTime.MAX);
        List<SeckillSessionEntity> sessions = baseMapper.getRecentThreeDaysSessionsWithSkus(startTime, endTime);
        if (sessions != null && sessions.size() > 0) {
            List<SeckillSessionWithSkusTo> collect = sessions.stream().map(session -> {
                SeckillSessionWithSkusTo sessionWithSkus = new SeckillSessionWithSkusTo();
                Long sessionId = session.getId();
                List<SeckillSkuRelationEntity> list = seckillSkuRelationService.list(new LambdaQueryWrapper<SeckillSkuRelationEntity>().eq(SeckillSkuRelationEntity::getPromotionSessionId, sessionId));
                List<SeckillSkuInfoTo> seckillSkus = list.stream().map(seckillSkuRelationEntity -> {
                    SeckillSkuInfoTo seckillSkuInfoTo = new SeckillSkuInfoTo();
                    BeanUtils.copyProperties(seckillSkuRelationEntity, seckillSkuInfoTo);
                    seckillSkuInfoTo.setStartTime(session.getStartTime());
                    seckillSkuInfoTo.setEndTime(session.getEndTime());
                    //远程查询sku详细信息
                    R res = productFeignService.info(seckillSkuInfoTo.getSkuId());
                    if (res != null && res.getCode() == 0) {
                        SkuInfoTo data = res.getData(new TypeReference<SkuInfoTo>() {
                        });
                        log.info("SeckillSessionServiceImpl,远程查询skuInfo成功...");
                        seckillSkuInfoTo.setSkuInfo(data);
                    }
                    return seckillSkuInfoTo;
                }).collect(Collectors.toList()); BeanUtils.copyProperties(session, sessionWithSkus);
                sessionWithSkus.setSeckillSkus(seckillSkus);
                return sessionWithSkus;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * 获取时间字符串
     * @return
     */
    private String getTime(LocalDate localDate,LocalTime localTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.of(localDate, localTime).format(formatter);
    }

}