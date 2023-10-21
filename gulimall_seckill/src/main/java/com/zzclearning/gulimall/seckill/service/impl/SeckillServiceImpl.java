package com.zzclearning.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zzclearning.common.utils.R;
import com.zzclearning.gulimall.seckill.feign.CouponFeignService;
import com.zzclearning.gulimall.seckill.interceptor.SeckillInterceptor;
import com.zzclearning.gulimall.seckill.schduled.SkuSecKillScheduler;
import com.zzclearning.gulimall.seckill.service.SeckillService;
import com.zzclearning.to.MemberEntityVo;
import com.zzclearning.to.seckill.SeckillSessionWithSkusTo;
import com.zzclearning.to.seckill.SeckillSkuInfoTo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author bling
 * @create 2023-02-25 15:41
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redisson;
    @Override
    public List<SeckillSessionWithSkusTo> getRecentThreeDaysSessionWithSkus() {
        R res = couponFeignService.getRecentThreeDaysSessionsWithSkus();
        if (res.getCode() == 0) {
            List<SeckillSessionWithSkusTo> data = res.getData(new TypeReference<List<SeckillSessionWithSkusTo>>() {
            });
            return data;
        }
        return null;

    }

    @Override
    public List<SeckillSkuInfoTo> getCurrentSeckillSkus() {
        Set<String> keys = redisTemplate.keys(SkuSecKillScheduler.SECKILL_SESSION_PREFIX + "*");
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                String substring = key.substring(SkuSecKillScheduler.SECKILL_SESSION_PREFIX.length() - 1);
                String[] split = substring.split("_");
                long startTime = Long.parseLong(split[0]);
                long endTime = Long.parseLong(split[1]);
                long curTime = System.currentTimeMillis();
                if (curTime >= startTime && curTime <= endTime) {
                    //获取当前场次对应的skuId列表 sessionId_skuId
                    List<String> range = redisTemplate.boundListOps(key).range(0, -1);
                    if (range!= null && range.size() > 0) {
                        BoundHashOperations<String, String, String> skuOps = redisTemplate.boundHashOps(SkuSecKillScheduler.SECKILL_SKUS);
                        List<String> stringList = skuOps.multiGet(range);
                        if (stringList != null && stringList.size() > 0) {
                            List<SeckillSkuInfoTo> collect = stringList.stream().map(s -> JSON.parseObject(s, new TypeReference<SeckillSkuInfoTo>() {
                            })).collect(Collectors.toList());
                            return collect;
                        }
                    }
                }

            }
        }
        return null;
    }

    @Override
    public SeckillSkuInfoTo getSkuSeckillInfo(Long skuId) {
        SeckillSkuInfoTo skuInfoTo;
        BoundHashOperations<String, String, String> skuOps = redisTemplate.boundHashOps(SkuSecKillScheduler.SECKILL_SKUS);
        Set<String> keys = skuOps.keys();
        // sessionId_skuId -->  seckillSkuInfo
        if (keys != null && keys.size() > 0) {
            //获取包含当前商品skuId的key
            List<String> collect = keys.stream().filter(key -> key.split("_")[1].equals(skuId.toString())).collect(Collectors.toList());
            List<String> stringList = skuOps.multiGet(collect);
            if (stringList != null && stringList.size() > 0) {
                //获取商品当前是否参与秒杀，否-->最近场次秒杀信息
                Optional<SeckillSkuInfoTo> first = stringList.stream().map(skuString -> JSON.parseObject(skuString, new TypeReference<SeckillSkuInfoTo>() {
                })).filter(sku -> sku.getEndTime().getTime() > System.currentTimeMillis()) //排除已经结束的秒杀活动
                        .min(Comparator.comparingLong(o -> o.getStartTime().getTime()));//获取最近的秒杀信息
                first.ifPresent(f-> {
                    if (f.getStartTime().getTime() <= System.currentTimeMillis()) {
                        f.setIsKill(true);//正在参与秒杀
                    } else {
                        f.setRandomCode(null);//不在秒杀时间，不返回随机码
                    }
                });
                return first.orElse(null);

            }
        }
       return null;
    }

    @Override
    public void killSku(String killId, String key, Integer num) {
        String skuString = (String)redisTemplate.boundHashOps(SkuSecKillScheduler.SECKILL_SKUS).get(killId);
        if (!StringUtils.isEmpty(skuString)) {
            SeckillSkuInfoTo seckillSku = JSON.parseObject(skuString, new TypeReference<SeckillSkuInfoTo>() {
            });
            long curTime = System.currentTimeMillis();
            //检查当前时间是否处于秒杀时间
            if (seckillSku.getStartTime().getTime() <= curTime && seckillSku.getEndTime().getTime() > curTime) {
                //检查商品随机码是否正确
                if (key.equals(seckillSku.getRandomCode())) {
                    //检查是否超过限购数量
                    if (num <= seckillSku.getSeckillLimit()) {
                        //检查用户是否已购买 userId_sessionId_skuId -->  num ,过期时间ttl = endTime - time
                        MemberEntityVo userInfo = SeckillInterceptor.loginUser.get();
                        Boolean isBought = redisTemplate.boundSetOps(SkuSecKillScheduler.SECKILL_USER).isMember(userInfo.getId().toString());
                        if (Boolean.FALSE.equals(isBought)) {
                            //获取信号量
                            RSemaphore semaphore = redisson.getSemaphore(SkuSecKillScheduler.SECKILL_SKU_CODE_PREFIX + key);
                            boolean success = semaphore.tryAcquire(num);
                            if (success) {
                                //秒杀成功,将用户id放入set中
                                redisTemplate.boundSetOps(SkuSecKillScheduler.SECKILL_USER).add(userInfo.getId().toString());
                                //给rabbitMQ发送消息进行后续下单等其他操作 --- 削峰
                                String orderSn = IdWorker.getTimeId();
                            }
                        }
                    }
                }
            }
        }


    }


}
