package com.zzclearning.gulimall.member.service.impl;

import com.zzclearning.gulimall.member.constant.MemberConst;
import com.zzclearning.gulimall.member.entity.MemberLevelEntity;
import com.zzclearning.gulimall.member.exception.UserNameExistException;
import com.zzclearning.gulimall.member.exception.UserPhoneExistException;
import com.zzclearning.gulimall.member.service.MemberLevelService;
import com.zzclearning.to.MemberRegisterLoginTo;
import com.zzclearning.to.SocialUserTo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.Query;

import com.zzclearning.gulimall.member.dao.MemberDao;
import com.zzclearning.gulimall.member.entity.MemberEntity;
import com.zzclearning.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    MemberLevelService memberLevelService;
    @Autowired
    private RedissonClient redisson;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void registerUser(MemberRegisterLoginTo memberRegisterLoginTo) {
        //对电话号码加锁，减小锁的粒度
        RLock lock = redisson.getLock(MemberConst.MEMBER_LOCK_KEY_REGISTER_PRE + memberRegisterLoginTo.getPhone());
        try {
            lock.lock(30L, TimeUnit.SECONDS);
            //查询用户名是否被注册
            String userName = memberRegisterLoginTo.getUserName();
            CheckUserNameUnique(userName);
            //查询手机号是否被注册
            String phone = memberRegisterLoginTo.getPhone();
            CheckPhoneUnique(phone);
            // 密码使用MD5盐值加密,自动加随机盐，盐值和 密码一起保存
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String encode = passwordEncoder.encode(memberRegisterLoginTo.getPassword());
            //获取用户默认等级
            MemberLevelEntity memberLevel = memberLevelService.getOne(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1));
            //保存用户注册信息
            MemberEntity member = new MemberEntity();
            member.setUsername(userName);
            //默认使用用户名作为昵称
            member.setNickname(userName);
            member.setMobile(phone);
            member.setLevelId(memberLevel.getId());
            member.setPassword(encode);
            this.save(member);
        } finally {
            lock.unlock();
        }


    }

    @Override
    public MemberEntity loginValid(MemberRegisterLoginTo memberRegisterLoginTo) {
        MemberEntity user = this.getOne(new QueryWrapper<MemberEntity>().eq("username", memberRegisterLoginTo.getLoginacct()));
        if (user == null) {
            return null;
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(memberRegisterLoginTo.getPassword(), user.getPassword()) ? user : null;
    }
    @Override
    public MemberEntity oauth2LoginValid(SocialUserTo socialUser) {
        //查看用户是否已注册
        MemberEntity socialMember = this.getOne(new QueryWrapper<MemberEntity>().eq("social_uid", socialUser.getSocialUid()));
        if (socialMember == null) {
            //用户未注册直接进行注册并登录，使用access_token查询用户信息 HttpUtil.doGet(...)
            String nickName = "random_" + UUID.randomUUID().toString().substring(0,4);
            MemberEntity regist = new MemberEntity();
            //会员默认等级
            MemberLevelEntity memberLevel = memberLevelService.getOne(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1));
            regist.setLevelId(memberLevel.getId());
            regist.setNickname(nickName);
            regist.setSocialUid(socialUser.getSocialUid());
            regist.setAccessToken(socialUser.getAccessToken());
            regist.setExpiresIn(socialUser.getExpiresIn());
            this.save(regist);
            return regist;

        } else {
            //已注册 更新信息
            MemberEntity regist = new MemberEntity();
            regist.setId(socialMember.getId());
            regist.setAccessToken(socialUser.getAccessToken());
            regist.setExpiresIn(socialUser.getExpiresIn());
            this.updateById(regist);
            socialMember.setAccessToken(socialUser.getAccessToken());
            socialMember.setExpiresIn(socialUser.getExpiresIn());
            return socialMember;
        }
    }

    private void CheckPhoneUnique(String phone) throws UserPhoneExistException {
        MemberEntity userByPhone = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (userByPhone != null) {
            throw new UserPhoneExistException("该手机号已注册");
        }
    }

    private void CheckUserNameUnique(String userName) throws UserNameExistException {
        MemberEntity userByName = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (userByName != null) {
            throw new UserNameExistException("用户名已存在");
        }
    }

}