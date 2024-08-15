package com.atguigu.common.to;

import lombok.Data;

/**
 * ClassName: SkuHasStockVo
 * Package: com.atguigu.gulimall.ware.vo
 * Description:
 *
 * @Author: 邓桂材
 * @Create: 2024/8/15 -22:18
 * @Version: v1.0
 */
@Data
public class SkuHasStockVo {
    private Long skuId;
    private Boolean hasStock;
}
