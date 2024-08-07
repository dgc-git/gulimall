package com.atguigu.gulimall.product.vo;

import lombok.Data;

/**
 * @author dgc
 * @date 2024/8/7 9:10
 */
@Data
public class AttrReponseVo extends AttrVo{
    private String catelogName;
    private String goupName;
    private Long[] catelogPath;
}
