package com.zzclearning.gulimall.product.interceptor;

import com.alibaba.fastjson.JSON;
import com.zzclearning.common.constant.AuthServerConstant;
import com.zzclearning.to.MemberEntityVo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *  拦截item.com的web请求
 * @author bling
 * @since 2023-02-17 15:59
 */
@Component
public class ProductInterceptor implements HandlerInterceptor {
    public static final ThreadLocal<MemberEntityVo> userInfo = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {



        String token = request.getParameter("token");
        if (!StringUtils.isEmpty(token)) {
            //远程调用认证服务器获取用户信息，保存在session中
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> entity = restTemplate.getForEntity("http://auth.gulimall.com/userinfo?token=" + token, String.class);
            if (!StringUtils.isEmpty(entity.getBody())) {
                request.getSession().setAttribute(AuthServerConstant.LOGIN_USER, JSON.parseObject(entity.getBody(),MemberEntityVo.class));
            }
        }

        String requestURI = request.getRequestURI();
        //放行其他请求
        if (!requestURI.endsWith(".html") || requestURI.equals("index.html")) {
            return true;
        }
        //判断是否登录
        Object loginUser1 = request.getSession().getAttribute("loginUser");

        MemberEntityVo loginUser = (MemberEntityVo)request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (loginUser == null) {
            //用户未登录，转到登录界面
            response.sendRedirect("http://auth.gulimall.com/login.html?return_url=http://item.gulimall.com" + request.getRequestURI());
            return false;
        }
        userInfo.set(loginUser);
        return true;
    }
}
