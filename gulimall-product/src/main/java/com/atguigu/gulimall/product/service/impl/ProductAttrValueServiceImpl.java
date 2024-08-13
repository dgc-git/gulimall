package com.atguigu.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.ProductAttrValueDao;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
import org.springframework.transaction.annotation.Transactional;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    @Transactional
    public void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> entities) {
        //删除这个spuId之前对应的所有属性
        this.baseMapper.delete(new LambdaQueryWrapper<ProductAttrValueEntity>().eq(ProductAttrValueEntity::getSpuId,spuId));
        List<ProductAttrValueEntity> collect = entities.stream().peek(item -> item.setSpuId(spuId)).collect(Collectors.toList());
        this.saveBatch(collect);
    }

    @Override
    public List<ProductAttrValueEntity> baseAttrListforspu(Long spuId) {
        return this.baseMapper.selectList(new LambdaQueryWrapper<ProductAttrValueEntity>().eq(ProductAttrValueEntity::getSpuId, spuId));
    }

    @Override
    public void saveProductAttr(List<ProductAttrValueEntity> collect) {
        this.saveBatch(collect);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

}