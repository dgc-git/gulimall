package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 封装订单提交的数据
 */
@Data
public class OrderSubmitVo {
    private Long addrId;
    private Integer payType;//支付方式
    //无需提交需要购买的商品，去购物车再获取一次
    //优惠，发票
    private String orderToken;
    private BigDecimal payPrice;//验价
    //用户相关信息不用提交
    private String note;
}
