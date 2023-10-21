package com.zzclearning.gulimall.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;

@SpringBootTest
class GulimallOrderApplicationTests {
    @Autowired
    StringRedisTemplate redisTemplate;

    @Test
    void contextLoads() {
        int dayOfMonth = LocalDateTime.now().getDayOfMonth();
        redisTemplate.opsForValue().setBit("usr:10001",dayOfMonth-1,true);
        redisTemplate.opsForValue().getBit("usr:10001",dayOfMonth-1);
        System.out.println("用户是否签到");


    }

}
