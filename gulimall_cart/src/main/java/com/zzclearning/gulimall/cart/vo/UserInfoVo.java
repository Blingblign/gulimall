package com.zzclearning.gulimall.cart.vo;

import lombok.Data;

/**
 * @author bling
 * @create 2023-02-14 10:35
 */
@Data
public class UserInfoVo {
    private Long userId;
    private String userKey;
    private boolean hasUserKey;//判断用户是否有userKey
}
