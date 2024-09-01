package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
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
import org.springframework.transaction.annotation.Transactional;


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

    /**
     * 默认只要是运行时异常都会回滚
     * @param
     * @return
     */
    @Override
    @Transactional(rollbackFor = NoStockException.class)
    public Boolean orderLockStock(WareSkuLockVo vo) {
        //1.找到每个商品在哪个仓库都有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            //查询这个商品在哪里有库存
            QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).apply("(stock-stock_locked)>0").select("ware_id");//todo 散库存的情况
            List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(wrapper);
            List<Long> wareIds = wareSkuEntities.stream().map(WareSkuEntity::getWareId).collect(Collectors.toList());
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());
        for (SkuWareHasStock hasStock : collect) {
            Boolean skuStocked=false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                //没有仓库有这个商品的库存
                //其他商品都不用扣了
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                //成功返回1，否则为0
                Long count=wareSkuDao.lockSkuStock(skuId,wareId,hasStock.getNum());
                if(count==0){
                    //当前仓库锁失败，重试下一个仓库
                }else {
                    //成功
                    skuStocked=true;
                    break;
                }
            }
            if(!skuStocked){
                //所有仓库都没锁住
                throw new NoStockException(skuId);
            }
        }
        //2.锁定库存
        return true;
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
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
                if (info.getCode() == 0) {
                    Map<String, Object> map = (Map<String, Object>) info.get("skuInfo");
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