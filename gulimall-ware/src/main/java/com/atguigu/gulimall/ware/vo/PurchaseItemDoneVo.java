package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * ClassName: PurchaseItemDoneVo
 * Package: com.atguigu.gulimall.ware.vo
 * Description:
 *
 * @Author: 邓桂材
 * @Create: 2024/8/12 -23:27
 * @Version: v1.0
 */
@Data
public class PurchaseItemDoneVo {
    private Long itemId;
    private Integer status;
    private String reason;
}
