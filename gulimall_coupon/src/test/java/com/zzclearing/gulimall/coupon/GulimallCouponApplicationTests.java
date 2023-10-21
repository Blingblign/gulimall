package com.zzclearing.gulimall.coupon;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

//@SpringBootTest

class GulimallCouponApplicationTests {
    //@Scheduled(cron = "* ? * * * *")
    @Test
    void contextLoads() {
        LocalDate startDate = LocalDate.now();
        System.out.println(startDate);
        LocalDate endDate = startDate.plusDays(2);
        LocalTime max = LocalTime.MAX;
        System.out.println(max);
        LocalTime min = LocalTime.MIN;
        System.out.println(min);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startTime = LocalDateTime.of(startDate, min).format(formatter);
        System.out.println(startTime);
        String endTime = LocalDateTime.of(endDate,max).format(formatter);
        System.out.println(endTime);
        //System.out.println("hello");
    }

}
