package com.zzclearning.gulimall.ware.config;

import lombok.Data;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * 创建队列，交换机，延时队列，绑定关系 的configuration
 * 1.Broker中的Queue、Exchange、Binding不存在的情况下，会自动创建（在RabbitMQ），不会重复创建覆盖
 * 2.懒加载，只有第一次使用的时候才会创建（例如监听队列）
 */
@Configuration
public class MyRabbitMQConfig {
    public static final long STOCK_DELAY_TIME_EXPIRE = 240000;
    //以json格式存储消息
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    /**
     * 延时队列
     */
    @Bean
    public Queue stockDelayQueue() {
        /**
         * Queue(String name,  队列名字
         *       boolean durable,  是否持久化
         *       boolean exclusive,  是否排他
         *       boolean autoDelete, 是否自动删除
         *       Map<String, Object> arguments) 属性【TTL、死信路由、死信路由键】
         */
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");// 死信路由
        arguments.put("x-dead-letter-routing-key", "stock.release.stock");// 死信路由键
        arguments.put("x-message-ttl", STOCK_DELAY_TIME_EXPIRE); // 消息过期时间 2分钟
        return new Queue("stock.delay.queue", true, false, false, arguments);
    }

    /**
     * 交换机（死信路由）
     */
    @Bean
    public Exchange stockEventExchange() {
        return new TopicExchange("stock-event-exchange", true, false);
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue stockReleaseQueue() {
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    /**
     * 绑定：交换机与库存解锁延迟队列
     */
    @Bean
    public Binding stockLockBinding() {
        /**
         * String destination, 目的地（队列名或者交换机名字）
         * DestinationType destinationType, 目的地类型（Queue、Exhcange）
         * String exchange,
         * String routingKey,
         * Map<String, Object> arguments
         **/
        return new Binding("stock.delay.queue",
                           Binding.DestinationType.QUEUE,
                           "stock-event-exchange",
                           "stock.locked",
                           null);
    }

    /**
     * 绑定：交换机与库存解锁死信队列
     */
    @Bean
    public Binding stockReleaseBinding() {
        return new Binding("stock.release.stock.queue",
                           Binding.DestinationType.QUEUE,
                           "stock-event-exchange",
                           "stock.release.#",
                           null);
    }
}
