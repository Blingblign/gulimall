package com.zzclearning.gulimall.order.vo;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.zzclearning.to.MemberReceiveAddressTo;
import com.zzclearning.to.SkuHaStockVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author bling
 * @create 2023-02-17 16:07
 */
@Data
public class ConfirmOrderVo {
    private List<MemberReceiveAddressTo> memberAddressVos;//用户收货地址列表
    private Integer                      count;//商品总数量
    private BigDecimal                   total;//总商品金额
    private BigDecimal                   payPrice = new BigDecimal(0);//应付总额,让页面自己发送请求去查询邮费然后计算得出
    private String uniqueToken;//防刷令牌，保证幂等性
    private List<OrderItem> items;//选中的购物项列表
    private Map<Long, Boolean> stocks;//查看商品是否有货(暂不支持从其他仓库调货)
}
