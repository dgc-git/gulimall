package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * ClassName: SkuReductionTo
 * Package: com.atguigu.common.to
 * Description:
 *
 * @Author: 邓桂材
 * @Create: 2024/8/8 -22:56
 * @Version: v1.0
 */
@Data
public class SkuReductionTo {
    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
}
