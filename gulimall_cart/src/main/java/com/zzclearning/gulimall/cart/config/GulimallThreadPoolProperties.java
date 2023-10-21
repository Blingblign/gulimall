package com.zzclearning.gulimall.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author bling
 * @create 2023-02-09 0:03
 */
@Data
@ConfigurationProperties(prefix = "gulimall.thread")
public class GulimallThreadPoolProperties {
    private int coreSize;
    private int maxSize;
    private Long keepAliveTime;
}
