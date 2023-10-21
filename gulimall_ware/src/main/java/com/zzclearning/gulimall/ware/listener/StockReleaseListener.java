package com.zzclearning.gulimall.ware.listener;

import com.rabbitmq.client.Channel;
import com.zzclearning.gulimall.ware.service.WareSkuService;
import com.zzclearning.to.mq.StockLockedTo;
import com.zzclearning.vo.SkuLockStockVo;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author bling
 * @create 2023-02-20 20:18
 */
@Component
@RabbitListener(queues = {"stock.release.stock.queue"})
public class StockReleaseListener {
    @Autowired
    WareSkuService wareSkuService;
    /**
     * 订单解锁库存
     * @param orderSn
     * @param channel
     */
    @RabbitHandler
    public void handleOrderReleaseLock(String orderSn, Channel channel, Message message) throws IOException {
        System.out.println("监听到订单解锁库存消息，订单号为" + orderSn);

        //手动ack
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            wareSkuService.handleOrderLock(orderSn);
            channel.basicAck(deliveryTag,false);
        } catch (Exception e) {
            //只要有异常，让消息重新入队
            e.printStackTrace();
            channel.basicReject(deliveryTag,true);
        }
    }

    /**
     * 库存超时自动解锁
     * @param stockLockedTo
     * @param channel
     */
    @RabbitHandler
    public void handleStockReleaseLock(StockLockedTo stockLockedTo, Channel channel, Message message) throws IOException {
        System.out.println("监听到库存超时自动解锁消息，" + stockLockedTo);

        //手动ack
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            wareSkuService.handleStockLock(stockLockedTo);
            channel.basicAck(deliveryTag,false);
        } catch (Exception e) {
            e.printStackTrace();
            channel.basicReject(deliveryTag,true);
        }
    }
}
