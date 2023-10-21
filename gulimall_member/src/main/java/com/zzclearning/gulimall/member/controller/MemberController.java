package com.zzclearning.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.zzclearning.common.exception.BizExceptionEnum;
import com.zzclearning.gulimall.member.exception.UserNameExistException;
import com.zzclearning.gulimall.member.exception.UserPhoneExistException;
import com.zzclearning.to.MemberRegisterLoginTo;
import com.zzclearning.to.SocialUserTo;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zzclearning.gulimall.member.entity.MemberEntity;
import com.zzclearning.gulimall.member.service.MemberService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.R;



/**
 * 会员
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 22:07:05
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;


    /**
     * 列表
     */
    @RequestMapping("/list")
        public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 根据用户名或手机查询用户是否已注册
     */
    @PostMapping("/register")
    public R registerUser(@RequestBody MemberRegisterLoginTo memberRegisterLoginTo) {
        try {
            memberService.registerUser(memberRegisterLoginTo);
        } catch (UserPhoneExistException e) {
            return R.error(BizExceptionEnum.USER_PHONE_EXIST_EXCEPTION.getCode(), BizExceptionEnum.USER_PHONE_EXIST_EXCEPTION.getMessage());
        } catch (UserNameExistException e) {
            return R.error(BizExceptionEnum.USER_NAME_EXIST_EXCEPTION.getCode(), BizExceptionEnum.USER_NAME_EXIST_EXCEPTION.getMessage());
        }
        return R.ok();
    }

    /**
     * 普通登录，校验用户密码是否正确
     * @param memberRegisterLoginTo
     * @return
     */
    @PostMapping("/login")
    public R login(@RequestBody MemberRegisterLoginTo memberRegisterLoginTo) {
        MemberEntity member = memberService.loginValid(memberRegisterLoginTo);
        return member!=null ? R.ok().setData(member) : R.error(BizExceptionEnum.USER_INVALID_EXCEPTION.getCode(), BizExceptionEnum.USER_INVALID_EXCEPTION.getMessage());
    }
    @PostMapping("/oauth2/login")
    public R oauth2Login(@RequestBody SocialUserTo socialUser) {
        MemberEntity member = memberService.oauth2LoginValid(socialUser);
        return R.ok().setData(member);
    }
}
