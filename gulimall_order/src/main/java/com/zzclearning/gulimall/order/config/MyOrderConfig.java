package com.zzclearning.gulimall.order.config;

import com.rabbitmq.client.AMQP;
import com.zzclearning.gulimall.order.interceptor.OrderInterceptor;
import feign.RequestInterceptor;
import org.springframework.amqp.core.Binding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;

/**
 * @author bling
 * @create 2023-02-17 15:58
 */
@Configuration
public class MyOrderConfig implements WebMvcConfigurer {
    //配置拦截器
    @Autowired
    OrderInterceptor orderInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(orderInterceptor).addPathPatterns("/**");
    }
    //feign的请求拦截器，用于添加请求头信息
    @Bean
    public RequestInterceptor requestInterceptor() {
        return (template) ->{
            //从请求上下文中获取请求头信息
            ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String cookie = request.getHeader("Cookie");
                template.header("Cookie",cookie);
            }
        };
    }
}
