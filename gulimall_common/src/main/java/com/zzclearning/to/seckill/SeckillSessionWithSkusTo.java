package com.zzclearning.to.seckill;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 *
 * @author bling
 * @create 2023-02-25 15:44
 */
@Data
public class SeckillSessionWithSkusTo {

    private List<SeckillSkuInfoTo> seckillSkus;//该场次秒杀商品id列表
    /**
     * 场次id
     */
    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date   startTime;
    /**
     * 每日结束时间
     */
    private Date   endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;
}
