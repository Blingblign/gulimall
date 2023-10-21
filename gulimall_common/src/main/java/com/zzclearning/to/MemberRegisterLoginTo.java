package com.zzclearning.to;

import lombok.Data;
import org.springframework.core.annotation.AliasFor;

/**
 * @author bling
 * @create 2023-02-10 8:59
 */
@Data
public class MemberRegisterLoginTo {

    private String userName;
    private String loginacct;
    private String password;
    private String phone;
}
