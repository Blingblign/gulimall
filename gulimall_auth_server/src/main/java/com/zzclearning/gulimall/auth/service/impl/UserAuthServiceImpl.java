package com.zzclearning.gulimall.auth.service.impl;

import com.zzclearning.gulimall.auth.feign.MemberFeignService;
import com.zzclearning.gulimall.auth.feign.ThirdPartFeignService;
import com.zzclearning.gulimall.auth.service.UserAuthService;
import com.zzclearning.gulimall.auth.vo.SmsCodeVo;
import com.zzclearning.gulimall.auth.vo.UserRegisterVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author bling
 * @create 2023-02-09 16:55
 */
@Slf4j
@Service
public class UserAuthServiceImpl implements UserAuthService {
    @Autowired
    StringRedisTemplate   redisTemplate;
    @Autowired
    ThirdPartFeignService thirdPartFeignService;
    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public void getCode(String phone) {
        long curTime = System.currentTimeMillis();
        //1.验证码在redis中的key--> sms:code:phone,value--> code_当前时间;
        String codeDb = redisTemplate.opsForValue().get("sms:code:" + phone);
        if (!StringUtils.isEmpty(codeDb)) {
            //2.获取验证码时间必须大于1分钟
            long saveTime = Long.parseLong(codeDb.split("_")[1]);
            if (curTime - saveTime < 60000) {
                throw new RuntimeException("验证码查询太频繁");
            }
            //删除未过期验证码
            redisTemplate.delete("sms:code:" + phone);
        }
        String code = UUID.randomUUID().toString().substring(0, 6);
        String redisString = code +"_"+ System.currentTimeMillis();
        //在缓存中存储验证码，十分钟过期
        redisTemplate.opsForValue().set("sms:code:" + phone, redisString, 10, TimeUnit.MINUTES);
        //调用第三方服务发送验证码
        try {
            thirdPartFeignService.getCode(new SmsCodeVo(phone, code));
        } catch (Exception e) {
            log.error("调用第三方服务发送验证码失败...",e);
        }
    }

    @Override
    public void register(UserRegisterVo userInfo) {
        //校验验证码
        //String codeUser = userInfo.getCode();
        //String codeDb = redisTemplate.opsForValue().get("sms:code:" + userInfo.getPhone());
        //if (codeDb == null || !codeDb.equals(codeUser)) {
        //    throw  new CodeIncorrectException("验证码错误");
        //}
        ////查看昵称和手机号是否重复,远程调用会员服务
        //MemberRegisterLoginTo memberRegisterTo = new MemberRegisterLoginTo();
        //BeanUtils.copyProperties(userInfo, memberRegisterTo);
        //R result = memberFeignService.registerUser(memberRegisterTo);
        //if (result.getCode() == 0) {
        //
        //}
    }
}
