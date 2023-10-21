package com.zzclearning.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 用户注册
 * @author bling
 * @create 2023-02-09 18:11
 */
@Data
public class UserRegisterVo {
    @NotBlank(message = "用户名不能为空")
    @Length(max = 12,min = 6,message = "用户名长度必须为6-12位")
    private String userName;
    @NotBlank(message = "密码不能为空")
    @Length(max = 12,min = 6,message = "密码长度必须为6-12位")
    private String password;
    @Pattern(regexp = "^1[3-9]\\d{9}$",message = "手机号格式错误")
    private String phone;
    @NotBlank(message = "验证码不能为空")
    @Length(max = 6,min = 6,message = "验证码错误")
    private String code;
}
