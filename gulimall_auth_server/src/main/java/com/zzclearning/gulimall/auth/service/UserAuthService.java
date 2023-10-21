package com.zzclearning.gulimall.auth.service;

import com.zzclearning.gulimall.auth.vo.UserRegisterVo;

/**
 * @author bling
 * @create 2023-02-09 16:55
 */
public interface UserAuthService {
    /**
     * 获取验证码
     * @param phone
     */
    void getCode(String phone);

    void register(UserRegisterVo userInfo);
}
