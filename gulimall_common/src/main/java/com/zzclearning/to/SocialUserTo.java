package com.zzclearning.to;

import lombok.Data;

/**
 * @author bling
 * @create 2023-02-10 17:23
 */
@Data
public class SocialUserTo {
    /**
     * 社交用户的唯一id
     */
    private String socialUid;
    /**
     * 访问令牌
     */
    private String accessToken;
    /**
     * 访问令牌的时间
     */
    private String expiresIn;

}
