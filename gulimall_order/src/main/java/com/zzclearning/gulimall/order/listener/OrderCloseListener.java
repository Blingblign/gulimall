package com.zzclearning.gulimall.order.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.Channel;
import com.zzclearning.common.constant.OrderStatusEnum;
import com.zzclearning.gulimall.order.config.AlipayTemplate;
import com.zzclearning.gulimall.order.entity.OrderEntity;
import com.zzclearning.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author bling
 * @create 2023-02-20 17:30
 */
@Slf4j
@Component
@RabbitListener(queues={"order.release.order.queue"})
public class OrderCloseListener {
    @Autowired
    OrderService orderService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    AlipayTemplate alipayTemplate;

    @RabbitHandler
    public void closeOrder(Message message, String orderSn, Channel channel) throws IOException {
        log.info("监听到订单过期消息，订单号为" + orderSn + LocalDateTime.now());
        try {
            // TODO 手动向支付宝发送关单请求--不可行
            //解决方案：设置订单自动解锁时间为40min,支付宝支付过期时间10分钟，前台显示订单时间为30min过期--->设置订单超时绝对时间
            orderService.closeOrder(orderSn);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);

        } catch (Exception e) {
            e.printStackTrace();
            //有异常，消息重新入队
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }
}
