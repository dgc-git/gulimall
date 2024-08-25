package com.atguigu.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * ClassName: SpuItemAttrGroupVo
 * Package: com.atguigu.gulimall.product.vo
 * Description:
 *
 * @Author: 邓桂材
 * @Create: 2024/8/25 -10:25
 * @Version: v1.0
 */
@Data
@ToString
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<Attr> attrs;
}
