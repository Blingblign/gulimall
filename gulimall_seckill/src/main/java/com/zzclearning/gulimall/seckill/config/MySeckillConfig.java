package com.zzclearning.gulimall.seckill.config;

import com.zzclearning.gulimall.seckill.interceptor.SeckillInterceptor;
import com.zzclearning.gulimall.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author bling
 * @create 2023-02-26 12:34
 */
@Configuration
public class MySeckillConfig implements WebMvcConfigurer {
    @Autowired
    SeckillInterceptor seckillInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(seckillInterceptor).addPathPatterns("/**");
    }
}
