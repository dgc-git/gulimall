package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * ClassName: MergeVo
 * Package: com.atguigu.gulimall.ware.vo
 * Description:
 *
 * @Author: 邓桂材
 * @Create: 2024/8/11 -17:45
 * @Version: v1.0
 */
@Data
public class MergeVo {
    private Long purchaseId;
    private List<Long> items;
}
