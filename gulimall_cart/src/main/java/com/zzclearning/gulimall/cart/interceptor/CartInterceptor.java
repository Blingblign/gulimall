package com.zzclearning.gulimall.cart.interceptor;

import com.zzclearning.common.constant.AuthServerConstant;
import com.zzclearning.gulimall.cart.constant.CartConstant;
import com.zzclearning.gulimall.cart.vo.UserInfoVo;
import com.zzclearning.to.MemberEntityVo;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @author bling
 * @create 2023-02-14 10:47
 */
public class CartInterceptor implements HandlerInterceptor {
    public static final ThreadLocal<UserInfoVo> threadLocal = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoVo userInfoVo = new UserInfoVo();
        //判断用户是否登录
        MemberEntityVo loginUser = (MemberEntityVo)request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (loginUser != null) {
            userInfoVo.setUserId(loginUser.getId());
        }
        if (request.getCookies() != null && request.getCookies().length > 0) {
            for (Cookie cookie : request.getCookies()) {
                if (CartConstant.USER_KEY.equals(cookie.getName())) {
                    userInfoVo.setUserKey(cookie.getValue());
                    userInfoVo.setHasUserKey(true);
                }
            }
        }

        //cookie中没有user-key
        if (!userInfoVo.isHasUserKey()) {
            userInfoVo.setUserKey(UUID.randomUUID().toString());
        }
        threadLocal.set(userInfoVo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //用户没有user-key,则生成一个user-key并添加到cookie中
        UserInfoVo userInfoVo = threadLocal.get();
        if (!userInfoVo.isHasUserKey()) {
            Cookie cookie = new Cookie(CartConstant.USER_KEY, userInfoVo.getUserKey());
            cookie.setMaxAge(60*60*24*30);//设置cookie一个月后过期
            response.addCookie(cookie);
        }
    }
}
