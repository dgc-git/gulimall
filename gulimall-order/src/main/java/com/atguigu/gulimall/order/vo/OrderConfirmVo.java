package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

public class OrderConfirmVo {
    @Getter
    @Setter
    List<MemberAddressVo> address;
    //所有选中的购物项
    @Getter
    @Setter
    List<OrderItemVo> items;
    //发票
    //优惠券信息
    @Getter
    @Setter
    private Integer integration;
    @Getter
    @Setter
    String orderToken;//方重令牌

//    BigDecimal total;//订单总额
//    BigDecimal payPrice;//应付价格

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null && items.size() > 0) {
            for (OrderItemVo item : items) {
                sum=sum.add(item.getPrice().multiply(new BigDecimal(item.getCount().toString())));
            }
        }
        return sum;
    }

    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
