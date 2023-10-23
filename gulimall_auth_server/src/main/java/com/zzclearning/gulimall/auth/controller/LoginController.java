package com.zzclearning.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zzclearning.common.constant.AuthServerConstant;
import com.zzclearning.common.exception.BizExceptionEnum;
import com.zzclearning.common.utils.R;
import com.zzclearning.gulimall.auth.constant.AuthConstant;
import com.zzclearning.gulimall.auth.feign.MemberFeignService;
import com.zzclearning.gulimall.auth.service.UserAuthService;
import com.zzclearning.to.MemberEntityVo;
import com.zzclearning.gulimall.auth.vo.UserLoginVo;
import com.zzclearning.gulimall.auth.vo.UserRegisterVo;
import com.zzclearning.to.MemberRegisterLoginTo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author bling
 * @create 2023-02-09 15:17
 */
@Controller
public class LoginController {
    @Autowired
    UserAuthService userAuthService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 转到登录界面
     * 单点登录结合springSession，实现一处登录，处处登录
     * redis中使用token作为key存储用户信息
     * 由于cookie不能跨域，通过认证服务器cookie保存token
     * @return
     */
    @GetMapping("/login.html")
    public String login(HttpSession session, @RequestParam(value = "return_url",required = false) String url,
                        Model model,
                        @CookieValue(value = "sso_token",required = false)String ssoToken) {
        //在gulimall.com域下的服务
        //Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        //if (attribute!=null) {
        //    return "redirect:url";
        //}
        //不在gulimall.com域下的服务
        if (!StringUtils.isEmpty(ssoToken)) {
            String userInfo = redisTemplate.opsForValue().get(ssoToken);
            if (userInfo != null) {
                //已经登陆过
                if (StringUtils.isEmpty(url)) return "redirect:http://gulimall.com" + "?token=" + ssoToken;
                return "redirect:"+ url + "?token=" + ssoToken;
            }
        }
        //向下传递回调url
        if (!StringUtils.isEmpty(url))
            model.addAttribute("return_url",url);
        return "login";
    }
    /**
     * 通过token获取用户信息
     */
    @ResponseBody
    @GetMapping("/userinfo")
    public String getUserInfoByToken(@RequestParam("token") String token) {
        return redisTemplate.opsForValue().get(token);

    }
    /**
     * 普通登录
     * 用户名，密码
     * @param userInfo
     * @param redirectAttributes
     * @param session
     * @return
     */
    @PostMapping("/login")
    public String login(UserLoginVo userInfo, RedirectAttributes redirectAttributes, HttpSession session,
                        HttpServletResponse response,
                        @RequestParam(value = "return_url",required = false) String url) {
        //调用远程会员服务校验密码
        R result = memberFeignService.loginValid(userInfo);
        if (result.getCode() != 0) {
            Map<String, String> errors = new HashMap<>();
           errors.put("msg",result.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            if (StringUtils.isEmpty(url))  return "redirect:http://auth.gulimall.com/login.html";
            return "redirect:http://auth.gulimall.com/login.html" + "?return_url=" + url;
        }
        //session.setAttribute(AuthServerConstant.LOGIN_USER,result.getData(new TypeReference<MemberEntityVo>(){}));
        //登录成功，生成随机token，将用户信息保存在redis中
        String ssoToken = UUID.randomUUID().toString().replace("-","");
        MemberEntityVo member = result.getData(new TypeReference<MemberEntityVo>() {
        });
        //保存并设置过期时间
        redisTemplate.opsForValue().set(ssoToken, JSON.toJSONString(member), AuthConstant.TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
        //设置cookie
        response.addCookie(new Cookie(AuthConstant.SSO_COOKIE_NAME,ssoToken));
        if (StringUtils.isEmpty(url))  return "redirect:http://gulimall.com?token=" + ssoToken;
        return "redirect:" + url + "?token=" + ssoToken;
    }

    @PostMapping("/register")
    public String register(@Validated UserRegisterVo userInfo, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            //用户注册信息校验,将错误信息放到重定向域中
            bindingResultHandle(bindingResult, redirectAttributes);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        //userAuthService.register(userInfo);
        //校验验证码
        String codeUser = userInfo.getCode();
        String codeDbString = redisTemplate.opsForValue().get("sms:code:" + userInfo.getPhone());
        if (codeDbString == null || !codeDbString.split("_")[0].equals(codeUser)) {
            //throw  new CodeIncorrectException("验证码错误");
            Map<String, String> errors = new HashMap<>();
            errors.put("code","验证码错误");
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        //删除缓存中的验证码
        redisTemplate.delete("sms:code:" + userInfo.getPhone());
        //远程调用会员服务进行注册
        MemberRegisterLoginTo memberRegisterLoginTo = new MemberRegisterLoginTo();
        BeanUtils.copyProperties(userInfo, memberRegisterLoginTo);
        //TODO 连接超时问题 如果会员服务正常注册，但授权服务连接超时，是否重复调用
        R result = memberFeignService.registerUser(memberRegisterLoginTo);
        if (result.getCode() != 0) {
            Map<String, String> errors = new HashMap<>();
            if (result.getCode() == 15003) {
                errors.put("userName",result.getData("msg",new TypeReference<String>(){}));

            } else {
                errors.put("phone",result.getData("msg",new TypeReference<String>(){}));
            }
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";

        }

        return "redirect:http://auth.gulimall.com/login.html";
    };

    /**
     * 获取验证码
     * @return
     */
    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R getCode(String phone) {
        //校验手机号
        if(phone ==null || !phone.matches("^1[3-9]\\d{9}$")) {
            //手机号格式错误
            return R.error(BizExceptionEnum.PHONE_INVALID_EXCEPTION.getCode(), BizExceptionEnum.PHONE_INVALID_EXCEPTION.getMessage());
        }
        try {
            userAuthService.getCode(phone);
        } catch (Exception e) {
            return R.error(BizExceptionEnum.CODE_QUERY_MUCH_EXCEPTION.getCode(), BizExceptionEnum.CODE_QUERY_MUCH_EXCEPTION.getMessage());
        }
        return R.ok();
    }

    private void bindingResultHandle(BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        Map<String, String> errors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(error -> {
            String field = error.getField();
            //当属性为空时，长度也不满足，一个field有两个值
            if (!errors.containsKey(field)) {
                errors.put(field, error.getDefaultMessage());
            }
        });
        redirectAttributes.addFlashAttribute("errors", errors);
    }
}
