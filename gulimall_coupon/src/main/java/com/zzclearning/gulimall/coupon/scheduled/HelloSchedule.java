package com.zzclearning.gulimall.coupon.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author bling
 * @create 2023-02-23 20:59
 */
@Slf4j
@Component
@EnableScheduling
@EnableAsync
public class HelloSchedule {
    @Async
    @Scheduled(cron = "*/1 * * ? * *")
    public void scheduleTask() throws InterruptedException {
        log.info("hello");
        TimeUnit.SECONDS.sleep(3);
    }
}
