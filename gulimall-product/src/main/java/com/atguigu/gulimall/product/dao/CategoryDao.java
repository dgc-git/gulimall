package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author dgc
 * @email 18280349792@163.com
 * @date 2024-08-02 11:24:36
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
