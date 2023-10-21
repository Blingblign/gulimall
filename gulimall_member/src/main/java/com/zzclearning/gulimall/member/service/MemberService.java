package com.zzclearning.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.gulimall.member.entity.MemberEntity;
import com.zzclearning.to.MemberRegisterLoginTo;
import com.zzclearning.to.SocialUserTo;

import java.util.Map;

/**
 * 会员
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 22:07:05
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void registerUser(MemberRegisterLoginTo memberRegisterLoginTo);

    MemberEntity loginValid(MemberRegisterLoginTo memberRegisterLoginTo);

    /**
     * 社交登录
     * @param socialUser
     * @return
     */
    MemberEntity oauth2LoginValid(SocialUserTo socialUser);
}

