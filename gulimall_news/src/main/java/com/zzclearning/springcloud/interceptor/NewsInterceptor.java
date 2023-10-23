package com.zzclearning.springcloud.interceptor;



import com.zzclearning.springcloud.vo.MemberEntityVo;

import io.micrometer.core.instrument.util.JsonUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author bling
 * @create 2023-02-17 15:59
 */
@Component
public class NewsInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = request.getParameter("token");
        if (!StringUtils.isEmpty(token)) {
            //远程调用认证服务器获取用户信息，保存在session中
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> entity = restTemplate.getForEntity("http://auth.gulimall.com/userinfo?token=" + token, String.class);
            request.getSession().setAttribute("loginUser", entity.getBody());
        }

        Object loginUser = request.getSession().getAttribute("loginUser");
        String requestURI = request.getRequestURI();
        //拦截获取新闻请求
        if ("/news".equals(requestURI) && loginUser == null) {
            //用户未登录，转到登录界面
            response.sendRedirect("http://auth.gulimall.com/login.html?return_url=http://news.com" + requestURI);
            return false;
        }

        return true;
    }
}
