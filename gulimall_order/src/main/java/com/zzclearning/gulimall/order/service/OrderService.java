package com.zzclearning.gulimall.order.service;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.gulimall.order.entity.OrderEntity;
import com.zzclearning.gulimall.order.vo.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 22:05:25
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    ConfirmOrderVo orderConfirm() throws ExecutionException, InterruptedException;

    SubmitResponseVo submitOrder(SubmitOrderVo submitOrderVo);

    void closeOrder(String orderSn);

    PayVo getPayVo(String orderSn);

    String handleAlipayNotify(AliPayAsyncVO aliPayAsyncVO, HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException;
}

