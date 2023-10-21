package com.zzclearning.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zzclearning.common.constant.OrderStatusEnum;
import com.zzclearning.gulimall.order.config.AlipayTemplate;
import com.zzclearning.gulimall.order.entity.OrderEntity;
import com.zzclearning.gulimall.order.service.OrderService;
import com.zzclearning.gulimall.order.vo.AliPayAsyncVO;
import com.zzclearning.gulimall.order.vo.PayVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author bling
 * @create 2023-02-23 11:44
 */
@Slf4j
@Controller
public class OrderPayController {
    @Autowired
    AlipayTemplate alipayTemplate;
    @Autowired
    OrderService orderService;

    @ResponseBody
    @GetMapping(value = "/html/pay",produces = "text/html")
    public String pay(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        //构造支付信息
        PayVo payVo = orderService.getPayVo(orderSn);
        if (payVo == null) {
            return "订单已失效，请重新下单";
        }
        String payInfo = alipayTemplate.pay(payVo);
        log.info("发起付款请求，请求结果为：" + payInfo);//响应交给页面，自动提交表单，来到支付页
        return payInfo;
    }



}
