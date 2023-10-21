package com.zzclearning.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.zzclearning.gulimall.order.vo.AliPayAsyncVO;
import com.zzclearning.gulimall.order.vo.PayVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author bling
 * @create 2023-02-23 11:32
 */
@Slf4j
@ConfigurationProperties(prefix = "alipay")
@Data
@Component
public class AlipayTemplate {
    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    public static String app_id = "2021000122612743";

    // 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCOxUQ2H0uOSgLz4mBgUrIbX4qdw3Y2bzaukMEgNe/T+3t1moA4jb77l1vcmT3cIbDCnL7yXqJqwuq5ehOkdqvipyJ9RBNOfzdiN95i4o0kBkn+Gns49TplkNqMGVBEx8CdmbKx0GzTUYh7KNrwmDhEswvUOzFe6Oepqx1Deid2gRyjknls98Q1La3z7h3WmhvVDKpc/Mod1TB8dbCZ+LDQ9v0UQfggaI0rUPYrFv4H5mVGt4B18MgY41KJfRqfchmkK5c+k2FiM94IXPQhFS6aiS9Rz0yuT+/rQOns9qoP+aRIjxzTrEHQAQdIes1B3xKSw7dmuF2Tybd3ocJppajrAgMBAAECggEAfHBQLeRFFLZJsIzld89cUiOKTEvrozJ4NNsSmaKpcNjHGRAbJi/WALBCbxpaIp5y1I2OmO4HHorZz+QpbrXIx3rpwsktEsQrX75WZVYVswqQP5yRPmnwBkqlUmtV3T1GE5e9euKxl9Z+4OoROpdSo/5zFv6o35KdKPNuRDwaXVyJXii94s+yKiSfn9xbrioWh/25Cbi5t87191NlnYyEa8ZDEar9j2AYNLvI6+eMrowyVYRRXJr9/xy4tZ+tGPIvPoYuBN641V5zr3k8LJedshuBrGTj9DtEodshcg/LRRJbZBTYDNdEh3kVTwKmgtyWANyfxgNFf7C219JQYYvH0QKBgQDPV4Mitk2B0fwfxbXxrYllXu81s0So7iCCgLyyHHWmJTfmgGVJdz+GI5zPJti89hx9ZPeEHVnkSnUMsFOSbjZ01aEMRt/0USNxF8U4cAbYTVAQCXouWrvONhifq8txYIXIDLT6AsXz7zw4OmV/+oozbJiBV2ITQSmuVQgqj6hyxwKBgQCwRoCQBIozyXqZH14LXIiOuhLNgBzhGwP4QG3QnvifPF2ivtvMYaA9qFNZwlDy7WA+J5RyelNZUsd54zMJ7tF/9u6o/7FbWtXIeRuxKRofXfPBpMk+u45a/u9k82gqa8ExfY+PnRYEvkw7O3kw0otNP1gEBim32YDlF2ibpMO0vQKBgE7AcnJww6eXVifLNHwC6bizAjBnUqDHMVngA8uyu4zLfhvykIYC4QHV7O1RV+kNoWyPOOPZMhdfoG2dW2C2s8BUk8LR+KYXW1rNKH9zLWZilxJAR0Pr+oEsUtXyrmb1vlppWSPMgdtQ9pJ0u7CMPImjqZkW0410ppwsd9mSjuxpAoGASmvpmWQhSQ32MbGZjehqSv/qMlK1XqB+Q5BIDL0hMHFXAv/bYmWRNpbbYdj1PirT6EnjlZVpnRui1iYDBV2Lm6AQUM9q/KvT+EiRVXGJ8tZCtG2y/UZIKsjZH2DYiJhnv1JIixSCGEyTeKp6Vw98+ocUzNYhRNqSXaZNZvDgI20CgYAPPk3pt/dIEs2hts/BC+/rge5qL8/HS0C9DtfRff0r+fEkf3V63c5Xd0bj0dIrudApwH8Vp0TNi6bmouvYyhsZ/Z70AhfQi88FWQjZ1OXfA64D7uNHBE7i8W4paU0AxYJ43psdmT7NFV7udMlBzSelfoVXmAzpCOWFdbDEe+alNw==";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApIvtOBjjz0QqHNA0r9xU2WTVBMX4DMDiKiqO0BguioeqUbe4QAXWs53wo8xjV/JMInCfc/UX5gpinnFnbWqIP80cTtCV5UhFFXwGVo2kBJaJrQjJ6xlIzsmy7owK7ben2mztJfhyYogZY4M+ZQKRQXYZLNRqWz0FLORi4fuca3ebe8W76vXcwnk+EGb3+dRMN1LOLovknJOhiARg2+TfdgRkLBU9s+WE572xLwRngoHEE9BnuLmU1nuvz7BHEcWv8LUviGvTgrseJ+C84HUKcTc5IeSom0Fw74MyoVCZCGSMNtTeJkknVaGyujQt8poW5mzfynvHkVdWdmsCTmL0awIDAQAB";

    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = "http://order.gulimall.com/alipay/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //public static String return_url = "http://工程公网访问地址/alipay.trade.page.pay-JAVA-UTF-8/return_url.jsp";
    public static String return_url = "http://order.gulimall.com/list";//支付成功直接转到支付列表页

    // 签名方式
    public static String sign_type = "RSA2";

    // 字符编码格式
    public static String charset = "utf-8";

    // 支付宝网关
    public static String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    /**
     * 构造alipay支付请求
     * 将结果响应以text/html的形式给客户端
     * @return
     */
    public String pay(PayVo payVo) throws AlipayApiException {
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl, app_id, merchant_private_key, "json", charset, alipay_public_key, sign_type);

        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = payVo.getOrderSn();
        //付款金额，必填
        String total_amount = payVo.getTotalAmount();
        //订单名称，必填
        String subject = payVo.getSubject();
        //商品描述，可空
        String body = payVo.getBody();
        //订单超时时间
        String timeOutExpire = payVo.getTimeExpire();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                                            + "\"total_amount\":\""+ total_amount +"\","
                                            + "\"subject\":\""+ subject +"\","
                                            + "\"body\":\""+ body +"\","
                                            + "\"time_expire\":\""+ timeOutExpire +"\","
                                            + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        //若想给BizContent增加其他可选请求参数，以增加自定义超时时间参数timeout_express来举例说明
        //alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
        //		+ "\"total_amount\":\""+ total_amount +"\","
        //		+ "\"subject\":\""+ subject +"\","
        //		+ "\"body\":\""+ body +"\","
        //		+ "\"timeout_express\":\"10m\","
        //		+ "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        //请求参数可查阅【电脑网站支付的API文档-alipay.trade.page.pay-请求参数】章节

        //请求
        String result = alipayClient.pageExecute(alipayRequest).getBody();
        return result;
    }

    /**
     * 验签
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    public boolean verifySign(HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException {
        //获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        Map<String,String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipay_public_key, charset, sign_type); //调用SDK验证签名
        return signVerified;
	/* 实际验证过程建议商户务必添加以下校验：
	1、需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
	2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
	3、校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
	4、验证app_id是否为该商户本身。
	*/
    //    if(signVerified) {//验证成功
    //        //商户订单号
    //        String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
    //
    //        //支付宝交易号
    //        String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
    //
    //        //交易状态
    //        String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");
    //
    //        if(trade_status.equals("TRADE_FINISHED")){
    //            //判断该笔订单是否在商户网站中已经做过处理
    //            //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
    //            //如果有做过处理，不执行商户的业务程序
    //
    //            //注意：
    //            //退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
    //        }else if (trade_status.equals("TRADE_SUCCESS")){
    //            //判断该笔订单是否在商户网站中已经做过处理
    //            //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
    //            //如果有做过处理，不执行商户的业务程序
    //
    //            //注意：
    //            //付款完成后，支付宝系统发送该交易状态通知
    //        }
    //
    //        out.println("success");
    //
    //    }else {//验证失败
    //        out.println("fail");
    //
    //        //调试用，写文本函数记录程序运行情况是否正常
    //        //String sWord = AlipaySignature.getSignCheckContentV1(params);
    //        //AlipayConfig.logResult(sWord);
    //    }
    //
    //    //——请在这里编写您的程序（以上代码仅作参考）——
    }
}
