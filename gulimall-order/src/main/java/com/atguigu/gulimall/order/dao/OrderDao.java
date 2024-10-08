package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单
 * 
 * @author dgc
 * @email 18280349792@163.com
 * @date 2024-08-03 16:45:20
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    void updateOrderPayedStatus(@Param("outTradeNo") String outTradeNo, @Param("code") Integer code);
}
