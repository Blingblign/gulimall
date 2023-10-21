package com.zzclearning.vo;

import com.zzclearning.to.MemberReceiveAddressTo;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 计算收货地址邮费
 * @author bling
 * @create 2023-02-17 16:30
 */
@Data
public class AddressFare {
    private BigDecimal fare;
    private MemberReceiveAddressTo address;
}
