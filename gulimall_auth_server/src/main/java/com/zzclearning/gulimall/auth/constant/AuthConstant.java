package com.zzclearning.gulimall.auth.constant;

/**
 * @author bling
 * @create 2023-10-22 10:32
 */
public class AuthConstant {
    public static final Long TOKEN_EXPIRE_TIME = 30 * 60L;//token过期时间30min
    public static final String SSO_COOKIE_NAME = "sso_token";//单点登录cookie的键名
}
