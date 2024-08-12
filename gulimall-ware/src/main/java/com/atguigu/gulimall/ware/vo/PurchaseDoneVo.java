package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * ClassName: PurchaseDoneVo
 * Package: com.atguigu.gulimall.ware.vo
 * Description:
 *
 * @Author: 邓桂材
 * @Create: 2024/8/12 -23:27
 * @Version: v1.0
 */
@Data
public class PurchaseDoneVo {
    @NotNull
    private Long id;
    private List<PurchaseItemDoneVo> items;

}
