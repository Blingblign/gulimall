package com.zzclearning.gulimall.order.web;

import com.zzclearning.gulimall.order.service.OrderService;
import com.zzclearning.gulimall.order.vo.ConfirmOrderVo;
import com.zzclearning.gulimall.order.vo.SubmitOrderVo;
import com.zzclearning.gulimall.order.vo.SubmitResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.jws.WebParam;
import java.util.concurrent.ExecutionException;

/**
 * 构建订单页：
 * 1.页面环境准备，nginx配置静态文件，网关配置路由路径，域名配置，mav
 * 2.数据模型Order：订单id,收货地址Address,订单项OrderItem,是否有货,邮费Fare,应付总额TotalAmount,
 * 订单确认页-->提交订单-->支付
 * @author bling
 * @create 2023-02-17 14:57
 */
@Controller
public class OrderWebController {
    @Autowired
    OrderService orderService;

    /**
     * 订单确认页
     * @return
     */
    @GetMapping("/toTrade")
    public String toOrderConfirmPage(Model model) throws ExecutionException, InterruptedException {
        ConfirmOrderVo confirmOrderVo = orderService.orderConfirm();
        model.addAttribute("confirmOrderData",confirmOrderVo);
        return "confirm";
    }

    /**
     * 生成订单、锁库存
     * @param submitOrderVo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(SubmitOrderVo submitOrderVo, Model model, RedirectAttributes redirectAttributes) {
        SubmitResponseVo submitResponseVo = orderService.submitOrder(submitOrderVo);
        if (submitResponseVo.getCode() == 0) {
            model.addAttribute("submitOrderResp",submitResponseVo);
            return "pay";
        }
        String msg = "订单提交失败：";
        switch (submitResponseVo.getCode()) {
            case 1 : msg += "订单已失效，请重新提交";break;
            case 2 : msg += "商品价格发生变化，请重新提交";break;
            case 3 : msg += "商品库存不足";break;
        }
        redirectAttributes.addFlashAttribute("msg",msg);
        return "redirect:http://order.gulimall.com/toTrade";

    }
    @GetMapping("/list")
    public String orderListPage() {
        return "list";
    }
    @GetMapping("/detail")
    public String orderDetailPage() {
        return "detail";
    }
}
