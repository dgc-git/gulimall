package com.atguigu.common.to.mq;

import lombok.Data;

import java.util.List;

@Data
public class StockLockedTo {
    private Long id;//库存工作单的id,本来需要根据订单号查到工作单id，再根据工作单id查到详情，但是外键是工作单id，所以直接给mq工作单id更简单
    private StockDetailTo detail;//工作单详情的所有id

}
