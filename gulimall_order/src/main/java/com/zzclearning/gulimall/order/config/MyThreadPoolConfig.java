package com.zzclearning.gulimall.order.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author bling
 * @create 2023-02-08 23:58
 */
@Configuration
@EnableConfigurationProperties({GulimallThreadPoolProperties.class})
public class MyThreadPoolConfig {
    @Bean
    public ThreadPoolExecutor gulimallThreadPoolExecutor(GulimallThreadPoolProperties threadPoolProperties) {
        return new ThreadPoolExecutor(threadPoolProperties.getCoreSize(),
                                      threadPoolProperties.getMaxSize(),
                                      threadPoolProperties.getKeepAliveTime(), TimeUnit.SECONDS,
                                      new LinkedBlockingQueue<>(100000),
                                      Executors.defaultThreadFactory(),
                                      new ThreadPoolExecutor.AbortPolicy());
    }
}
