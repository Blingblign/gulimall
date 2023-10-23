package com.zzclearning.springcloud.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author bling
 * @create 2022-10-19 20:38
 */
@Configuration
public class FeignConfig {
    //配置openfeign的日志功能
    @Bean
    public Logger.Level level() {
        return Logger.Level.FULL;
    }
}
