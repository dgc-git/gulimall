package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author dgc
 * @email 18280349792@163.com
 * @date 2024-08-03 18:01:25
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
