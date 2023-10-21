package com.zzclearning.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.zzclearning.common.constant.AuthServerConstant;
import com.zzclearning.common.utils.R;
import com.zzclearning.gulimall.auth.feign.MemberFeignService;
import com.zzclearning.to.MemberEntityVo;
import com.zzclearning.to.SocialUserTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 社交登录
 * @author bling
 * @create 2023-02-10 17:12
 */
@Controller
@Slf4j
public class Oauth2Controller {
    @Autowired
    MemberFeignService memberFeignService;
    /**
     * 微博账号登录的回调
     * @param code
     * @param session
     * @return
     */
    @GetMapping("/oauth2.0/weibo/success")
    public String oauth2Login(@RequestParam("code") String code, HttpSession session) {
        log.info("微博临时授权票据为{}",code);
        //TODO 1.根据临时授权票据code带上app_id,app_secret获取访问令牌access_token;HttpUtils.doPost(...)
        //2.获得social_uid,access_token,expires_in-->封装成SocialUser对象
        SocialUserTo socialUserTo = new SocialUserTo();
        socialUserTo.setSocialUid(code);
        socialUserTo.setAccessToken(UUID.randomUUID().toString().substring(0,13));
        socialUserTo.setExpiresIn(System.currentTimeMillis() + 600000 + "");
        //3.调用远程会员服务进行注册或登录
        R result = memberFeignService.oauth2Login(socialUserTo);
        if (result.getCode() == 0) {
            //4.使用redisSession存储用户信息
            session.setAttribute(AuthServerConstant.LOGIN_USER,result.getData(new TypeReference<MemberEntityVo>(){}));
            return "redirect:http://gulimall.com";

        } else {
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
