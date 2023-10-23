package com.zzclearning.springcloud.controller;


import com.zzclearning.springcloud.vo.MemberEntityVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * @author bling
 * @create 2022-10-19 17:52
 */
@Controller
public class OrderFeignController {




    @GetMapping({"/hello","/"})
    public String index() {
        return "hello.html";
    }

    /**
     * 前端页面请求接口
     * @return
     */
    @ResponseBody
    @GetMapping("/user")
    public String getUserinfo(HttpSession session) {

        return (String) session.getAttribute("loginUser");
    }

    @ResponseBody
    @GetMapping("/news")
    public String getNews() {
        return "<div style=\"font:20px 隶书;margin-left:30px;\">滚滚长江东逝水，浪花淘尽英雄，是非成败转头空， 青山依旧在， 几度夕阳红。 <br>白发渔樵江渚上，惯看秋月春风，一壶浊酒喜相逢， 古今多少事， 都付笑谈中。" +
                "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "--曹操</div>";
    }


}
