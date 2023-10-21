package com.zzclearning.gulimall.order.feign;

import com.zzclearning.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author bling
 * @create 2022-11-03 17:18
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {
    @GetMapping("member/memberreceiveaddress/address/{memberId}")
    R getUserAddresses(@PathVariable("memberId") Long memberId);
}
