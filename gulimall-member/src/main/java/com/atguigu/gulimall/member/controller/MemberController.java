package com.atguigu.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.feign.CouponFeignService;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 会员
 *
 * @author dgc
 * @email 18280349792@163.com
 * @date 2024-08-03 18:01:25
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;
    @Autowired
    CouponFeignService couponFeignService;
    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo memberRegistVo){
        try {
            memberService.regist(memberRegistVo);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMessage());
        } catch (UsernameExistException e) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMessage());
        }
        return R.ok();
    }
    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo){
        MemberEntity memberEntity=memberService.login(vo);
        if (memberEntity != null) {
        return R.ok().setData(memberEntity);

        }else {
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(),BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMessage());
        }
    }
    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R membercoupons = couponFeignService.membercoupons();
        return R.ok().put("member",memberEntity).put("coupons",membercoupons.get("coupons"));
    }
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
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
