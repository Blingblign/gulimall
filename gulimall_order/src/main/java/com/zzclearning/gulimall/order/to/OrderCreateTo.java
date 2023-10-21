package com.zzclearning.gulimall.order.to;

import com.zzclearning.gulimall.order.entity.OrderEntity;
import com.zzclearning.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author bling
 * @create 2023-02-20 11:41
 */
@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> orderItems;
    private BigDecimal payPrice;
}
