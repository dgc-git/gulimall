package com.atguigu.gulimall.coupon.dao;

import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author dgc
 * @email 18280349792@163.com
 * @date 2024-08-03 15:06:40
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
