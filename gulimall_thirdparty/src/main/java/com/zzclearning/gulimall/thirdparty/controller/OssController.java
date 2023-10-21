package com.zzclearning.gulimall.thirdparty.controller;

import com.aliyun.oss.OSS;
import com.zzclearning.common.utils.R;
import com.zzclearning.gulimall.thirdparty.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author bling
 * @create 2022-10-26 20:07
 */
@RestController
public class OssController {
    @Autowired
    private OssService ossService;

    @GetMapping("/thirdparty/oss")
    public R policy() {
        Map<String, String> policy = ossService.policy();
        return R.ok().put("data",policy);
    }
}
