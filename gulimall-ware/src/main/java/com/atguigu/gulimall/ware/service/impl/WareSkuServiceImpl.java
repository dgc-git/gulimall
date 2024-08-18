package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.common.to.SkuHasStockVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    WareSkuDao wareSkuDao;
    @Autowired
    ProductFeignService productFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<WareSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        String skuId = (String) params.get("skuId");
        queryWrapper.eq(StringUtils.isNotEmpty(skuId), WareSkuEntity::getSkuId, skuId);
        String wareId = (String) params.get("wareId");
        queryWrapper.eq(StringUtils.isNotEmpty(wareId), WareSkuEntity::getWareId, wareId);
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        wrapper.in("sku_id", skuIds)
                .groupBy("sku_id")
                .select("sku_id", "SUM(stock - stock_locked) as remainStock");//todo bug?
        // 执行查询并获取结果
        List<Map<String, Object>> resultMapList = baseMapper.selectMaps(wrapper);
        // 将结果映射到 VO 中
        return resultMapList.stream().map(resultMap -> {
             SkuHasStockVo vo = new SkuHasStockVo();
            vo.setSkuId((Long) resultMap.get("sku_id"));
            BigDecimal remainStock = (BigDecimal) resultMap.get("remainStock");
            vo.setHasStock(remainStock != null && remainStock.longValue() > 0);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //如果没有库存记录则新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new LambdaQueryWrapper<WareSkuEntity>().eq(WareSkuEntity::getSkuId, skuId).eq(WareSkuEntity::getWareId, wareId));
        if (entities == null || entities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            //远程查询skuname,如果失败，事务不需要回滚
            //1.try-catch
            //todo 还有什么办法不会滚
            try {
                R info = productFeignService.info(skuId);
                if(info.getCode()==0){
                    Map<String,Object> map = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) map.get("skuName"));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            wareSkuDao.insert(wareSkuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

}