package com.zzclearning.gulimall.seckill.interceptor;

import com.zzclearning.common.constant.AuthServerConstant;
import com.zzclearning.to.MemberEntityVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author bling
 * @create 2023-02-14 10:47
 */
@Component
public class SeckillInterceptor implements HandlerInterceptor {
    public static final ThreadLocal<MemberEntityVo> loginUser = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //秒杀商品时，判断用户是否登录
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        String requestURI = request.getRequestURI();
        boolean match = antPathMatcher.match(requestURI, "/kill");
        if (match) {
            MemberEntityVo user = (MemberEntityVo)request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
            if (user != null) {
                loginUser.set(user);
            } else {
                //重定向到登录界面
                response.sendRedirect("http://auth.gulimall.com/login.html");
                return false;
            }
        }

        return true;
    }

}
