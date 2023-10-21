package com.zzclearning.gulimall.thirdparty.controller;

import com.zzclearning.common.utils.R;
import com.zzclearning.gulimall.thirdparty.vo.SmsCodeVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author bling
 * @create 2023-02-09 16:38
 */
@Controller
@RequestMapping("/sms")
public class SmsController {
    /**
     * 向第三方短信发送平台提交手机号和验证码
     * @param codeVo
     * @return
     */
    @ResponseBody
    @PostMapping("/sendcode")
    public R getCode(@RequestBody SmsCodeVo codeVo) {
        System.out.println("[谷粒商城]手机号:" + codeVo.getPhone() + ",验证码：" + codeVo.getCode() + "(10分钟有效)。您正在进行用户注册，请勿将验证码告诉他人哦。");
        return R.ok();
    }
}
