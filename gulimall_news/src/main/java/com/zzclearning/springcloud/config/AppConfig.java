package com.zzclearning.springcloud.config;

import com.zzclearning.springcloud.interceptor.NewsInterceptor;
import com.zzclearning.springcloud.rule.MyRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author bling
 * @create 2023-10-13 11:14
 */
@Configuration(proxyBeanMethods = false)
public class AppConfig implements WebMvcConfigurer {
    @Autowired
    NewsInterceptor newsInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(newsInterceptor).addPathPatterns("/**");
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public MyRule myRule() {
        return new MyRule();
    }

}
