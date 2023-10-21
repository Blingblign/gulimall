package com.zzclearning.gulimall.order.feign;

import com.zzclearning.common.utils.R;
import com.zzclearning.vo.SkuLockStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author bling
 * @create 2023-02-06 11:47
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {
    @PostMapping("/ware/waresku/hasStock")
    R skusHasStock(@RequestBody List<Long> skuIds);

    @GetMapping("ware/wareinfo/fare")
    R calculateFare(@RequestParam("addrId") Long addrId);

    @PostMapping("/ware/waresku/lockStock")
    R lockStock(@RequestBody SkuLockStockVo skuLockStockVo);
}
