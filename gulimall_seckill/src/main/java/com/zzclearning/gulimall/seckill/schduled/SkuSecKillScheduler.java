package com.zzclearning.gulimall.seckill.schduled;

import com.alibaba.fastjson.JSON;
import com.zzclearning.gulimall.seckill.service.SeckillService;
import com.zzclearning.to.seckill.SeckillSessionWithSkusTo;
import com.zzclearning.to.seckill.SeckillSkuInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 商品定时任务
 * @author bling
 * @create 2023-02-23 21:41
 */
@Slf4j
@EnableScheduling
@EnableAsync
@Component
public class SkuSecKillScheduler {
    public static final String SECKILL_SESSION_PREFIX = "seckill:session:";//秒杀场次前缀
    public static final String SECKILL_SKUS = "seckill:skus";//秒杀商品信息键值
    public static final String SECKILL_SKU_CODE_PREFIX = "seckill:stock:";//商品随机码前缀
    public static final String SECKILL_USER = "seckill:user";//商品随机码前缀
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    SeckillService seckillService;
    /**
     * 定时任务--上架最近三天的活动商品到redis中
     * 避免商品重复上架--幂等性
     * 分布式锁
     *
     */
    @Scheduled(cron = "0/3 * * * * ?")//每天晚上3点上架秒杀商品
    @Async
    public void skuUpScheduleTask() {
        //分布式锁
        RLock lock = redissonClient.getLock("seckillUp:scheduler");
        lock.lock(2, TimeUnit.MINUTES);
        try {
            log.info("商品定时上架...");
            //获取最近三天需要上架秒杀的场次信息及对应的商品信息数据 √
            List<SeckillSessionWithSkusTo> sessions = seckillService.getRecentThreeDaysSessionWithSkus();
            //redis保存场次信息 seckill:session:startTime_endTime --> sessionId_skuIds[]; 1_2,1_3
            saveSessionWithSkusInRedis(sessions);
            //redis保存秒杀商品详情  seckill:skus -->   sessionId_skuId-> seckillSkuInfo:skuInfo,start_time,end_time,randomCode
            saveSecKillSkuInfosInRedis(sessions);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        //seckill:stock:randomCode --> skuNum
        //主页：获取当前时间段正在参与秒杀的商品  √
        //商品详情页：查询当前时间该商品是否参与秒杀，先查询当前时间段正在秒杀的商品
        //          没有在当前场次，商品秒杀预告：查询该商品最近场次的秒杀信息 seckill:skuId -->List<Session> {session_id: xx,start_time: xx,end_time: xx};
        //
        //预约功能：seckill:subscribe:count:sessionId_skuId--> count,
        //         seckill:subscribe:users:sessionId_skuId -->set<UserId>;
    }

    private void saveSecKillSkuInfosInRedis(List<SeckillSessionWithSkusTo> sessions) {
        BoundHashOperations<String, Object, Object> skuOps = redisTemplate.boundHashOps(SECKILL_SKUS);
        sessions.forEach(session -> {
            Map<String, String> map = session.getSeckillSkus().stream()
                    .peek(sku-> {
                        sku.setStartTime(session.getStartTime());
                        sku.setEndTime(session.getEndTime());
                        //设置商品随机码
                        String randomCode = UUID.randomUUID().toString().replace("-", "");
                        sku.setRandomCode(randomCode);
                        redisTemplate.opsForValue().set(SECKILL_SKU_CODE_PREFIX + randomCode, sku.getSeckillCount().toString());
                    })
                    .filter(seckillSkuInfoTo -> skuOps.get(session.getId() + "_" + seckillSkuInfoTo.getSkuId()) == null)//防止重复上架
                    .collect(Collectors.toMap(seckillSkuInfoTo -> session.getId() + "_" + seckillSkuInfoTo.getSkuId(), JSON::toJSONString));
            skuOps.putAll(map);
        });
    }

    private void saveSessionWithSkusInRedis(List<SeckillSessionWithSkusTo> sessions) {
        sessions.forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SECKILL_SESSION_PREFIX + startTime + "_" + endTime;
            Boolean persist = redisTemplate.persist(key);
            //防止重复上架
            if (persist == null || !persist) {
                BoundListOperations<String, String> sessionOps = redisTemplate.boundListOps(key);
                sessionOps.leftPushAll(session.getSeckillSkus().stream().map(seckillSku -> {
                    Long skuId = seckillSku.getSkuId();
                    return session.getId() + "_" + skuId;
                }).toArray(String[]::new));
            }

        });
    }
}
