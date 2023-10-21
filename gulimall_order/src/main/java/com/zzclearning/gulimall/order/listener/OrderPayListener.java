package com.zzclearning.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.zzclearning.gulimall.order.config.AlipayTemplate;
import com.zzclearning.gulimall.order.service.OrderService;
import com.zzclearning.gulimall.order.vo.AliPayAsyncVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

/**
 * @author bling
 * @create 2023-02-23 8:49
 */
@Slf4j
@Controller
public class OrderPayListener {

    @Autowired
    OrderService orderService;
    /**
     * 获取支付宝异步通知
     * @return
     */
    @ResponseBody
    @PostMapping("/alipay/notify")
    public String handleAlipayNotify(AliPayAsyncVO aliPayAsyncVO, HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException {
        //验签，修改订单状态，发送库存扣减消息
        log.info("收到支付宝异步通知:{}",aliPayAsyncVO);
        String result = orderService.handleAlipayNotify(aliPayAsyncVO,request);
        log.info("返回支付校验结果:{}",result);
        return result;
    }
}
