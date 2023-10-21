package com.zzclearning.gulimall.auth.feign;

import com.zzclearning.common.utils.R;
import com.zzclearning.gulimall.auth.vo.SmsCodeVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author bling
 * @create 2023-02-09 17:23
 */
@FeignClient("gulimall-thirdparty")
public interface ThirdPartFeignService {
    @PostMapping("/sms/sendcode")
    R getCode(@RequestBody SmsCodeVo codeVo);
}
