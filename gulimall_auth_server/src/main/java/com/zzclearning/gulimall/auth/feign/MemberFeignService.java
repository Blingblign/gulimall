package com.zzclearning.gulimall.auth.feign;

import com.zzclearning.common.utils.R;
import com.zzclearning.gulimall.auth.vo.UserLoginVo;
import com.zzclearning.to.MemberRegisterLoginTo;
import com.zzclearning.to.SocialUserTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author bling
 * @create 2023-02-09 19:32
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/register")
    R registerUser(@RequestBody MemberRegisterLoginTo memberRegisterLoginTo);

    @PostMapping("/member/member/login")
    R loginValid(@RequestBody UserLoginVo userLoginVo);

    @PostMapping("/member/member/oauth2/login")
    R oauth2Login(@RequestBody SocialUserTo socialUser);
}
