package com.zzclearning.to.mq;

import lombok.Data;
import lombok.ToString;

/**
 * @author bling
 * @create 2023-02-20 17:25
 */
@Data
@ToString
public class StockLockedTo {
    private Long id;//工作单id
    private Long detailId;//工作单详情id
}
