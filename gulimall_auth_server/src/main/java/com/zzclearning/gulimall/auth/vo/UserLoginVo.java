package com.zzclearning.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 用户登录
 * @author bling
 * @create 2023-02-10 11:21
 */
@Data
public class UserLoginVo {
    private String loginacct;
    private String password;
}
