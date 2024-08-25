package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * ClassName: SkuItemVo
 * Package: com.atguigu.gulimall.product.vo
 * Description:
 *
 * @Author: 邓桂材
 * @Create: 2024/8/25 -9:27
 * @Version: v1.0
 */
@Data
public class SkuItemVo {
    //1.sku基本信息   pms_sku_info
    private SkuInfoEntity info;
    private boolean hasStock=true;
    //2.sku图片信息 sku_images
    List<SkuImagesEntity> images;
    //3.获取spu的销售属性组合
    List<SkuItemSaleAttrVo> saleAttr;
    //4.获取spu的介绍
    SpuInfoDescEntity desp;
    //5.spu的规格参数信息
    private List<SpuItemAttrGroupVo> goupAttrs;




}
