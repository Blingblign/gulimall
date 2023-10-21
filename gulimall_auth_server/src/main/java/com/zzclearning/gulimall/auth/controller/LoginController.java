package com.zzclearning.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.zzclearning.common.constant.AuthServerConstant;
import com.zzclearning.common.exception.BizExceptionEnum;
import com.zzclearning.common.utils.R;
import com.zzclearning.gulimall.auth.feign.MemberFeignService;
import com.zzclearning.gulimall.auth.service.UserAuthService;
import com.zzclearning.to.MemberEntityVo;
import com.zzclearning.gulimall.auth.vo.UserLoginVo;
import com.zzclearning.gulimall.auth.vo.UserRegisterVo;
import com.zzclearning.to.MemberRegisterLoginTo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

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
     * @return
     */
    @GetMapping("/login.html")
    public String login(HttpSession session) {
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute!=null) {
            return "redirect:http://gulimall.com";
        }
        return "login";
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
    public String login(UserLoginVo userInfo, RedirectAttributes redirectAttributes, HttpSession session) {
        //调用远程会员服务校验密码
        R result = memberFeignService.loginValid(userInfo);
        if (result.getCode() != 0) {
            Map<String, String> errors = new HashMap<>();
           errors.put("msg",result.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
        session.setAttribute(AuthServerConstant.LOGIN_USER,result.getData(new TypeReference<MemberEntityVo>(){}));
        return "redirect:http://gulimall.com";
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
