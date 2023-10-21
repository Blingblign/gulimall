package com.zzclearning.gulimall.order.vo;

import com.zzclearning.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author bling
 * @create 2023-02-20 10:28
 */
@Data
public class SubmitResponseVo {
    private Integer code = 0;
    private OrderEntity order;
}
