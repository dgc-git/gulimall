package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

/**
 * ClassName: AttrGroupWithAttrsVo
 * Package: com.atguigu.gulimall.product.vo
 * Description:
 *
 * @Author: 邓桂材
 * @Create: 2024/8/7 -20:34
 * @Version: v1.0
 */
@Data
public class AttrGroupWithAttrsVo extends AttrGroupEntity {
    private List<AttrEntity> attrs;
}
