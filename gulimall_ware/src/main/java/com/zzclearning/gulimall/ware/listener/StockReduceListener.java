package com.zzclearning.gulimall.ware.listener;

import com.rabbitmq.client.Channel;
import com.zzclearning.gulimall.ware.service.WareSkuService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author bling
 * @create 2023-02-23 16:32
 */
@RabbitListener(queues = {"order.finish.ware.queue"})
public class StockReduceListener {
    @Autowired
    WareSkuService wareSkuService;
    /**
     * 监听订单完成扣减库存消息
     * @param orderSn
     * @param channel
     * @param message
     * @throws IOException
     */
    @RabbitHandler
    public void handleOrderFinish(String orderSn, Channel channel, Message message) throws IOException {
        System.out.println("监听到订单完成扣减库存消息，订单号为" + orderSn);

        //手动ack
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            wareSkuService.handleOrderFinish(orderSn);
            channel.basicAck(deliveryTag,false);
        } catch (Exception e) {
            //只要有异常，让消息重新入队
            e.printStackTrace();
            channel.basicReject(deliveryTag,true);
        }
    }
}
