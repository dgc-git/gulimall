package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * ClassName: SpuBoundsTo
 * Package: com.atguigu.common.to
 * Description:
 *
 * @Author: 邓桂材
 * @Create: 2024/8/8 -22:45
 * @Version: v1.0
 */
@Data
public class SpuBoundsTo {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
