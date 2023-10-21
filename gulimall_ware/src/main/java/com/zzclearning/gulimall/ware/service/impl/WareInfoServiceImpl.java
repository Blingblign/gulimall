package com.zzclearning.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzclearning.common.utils.R;
import com.zzclearning.gulimall.ware.feign.MemberFeignService;
import com.zzclearning.to.MemberReceiveAddressTo;
import com.zzclearning.vo.AddressFare;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.Query;

import com.zzclearning.gulimall.ware.dao.WareInfoDao;
import com.zzclearning.gulimall.ware.entity.WareInfoEntity;
import com.zzclearning.gulimall.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {
    @Autowired
    MemberFeignService memberFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        LambdaQueryWrapper<WareInfoEntity> wrapper = Wrappers.lambdaQuery();
        //关键字查询
        if (!StringUtils.isBlank(key)) {
             wrapper.like(WareInfoEntity::getId, key)
                    .or().like(WareInfoEntity::getName, key)
                    .or().like(WareInfoEntity::getAddress, key);
        }
        IPage<WareInfoEntity> page = this.page(new Query<WareInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public AddressFare calculateFare(Long addrId) {
        //远程调用会员服务
        R res = memberFeignService.info(addrId);
        if (res!= null && res.getCode() == 0) {
            MemberReceiveAddressTo address = res.getData("memberReceiveAddress", new TypeReference<MemberReceiveAddressTo>() {
            });
            //通过电话号码最后一位作为邮费
            BigDecimal fare = new BigDecimal(address.getPhone().substring(address.getPhone().length() - 1));
            AddressFare addressFare = new AddressFare();
            addressFare.setFare(fare);
            addressFare.setAddress(address);
            return addressFare;
        }
        return null;

    }

}