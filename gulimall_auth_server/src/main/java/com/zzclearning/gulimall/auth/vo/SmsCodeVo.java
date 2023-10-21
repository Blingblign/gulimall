package com.zzclearning.gulimall.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 给手机号发送短信
 * @author bling
 * @create 2023-02-09 17:26
 */
@Data
@AllArgsConstructor
public class SmsCodeVo {
    private String phone;
    private String code;
}
