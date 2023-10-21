package com.zzclearning.gulimall.order.interceptor;

import com.zzclearning.common.constant.AuthServerConstant;
import com.zzclearning.to.MemberEntityVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author bling
 * @create 2023-02-17 15:59
 */
@Component
public class OrderInterceptor implements HandlerInterceptor {
    public static final ThreadLocal<MemberEntityVo> userInfo = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //放行库存服务查询订单状态的请求
        boolean match = new AntPathMatcher().match("/order/order/orderInfo/**", request.getRequestURI());
        if (match) {
            return true;
        }
        MemberEntityVo loginUser = (MemberEntityVo)request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (loginUser == null) {
            //用户未登录，转到登录界面
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
        userInfo.set(loginUser);
        return true;
    }
}
