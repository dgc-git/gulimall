package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;

@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code;//错误状态码

}
