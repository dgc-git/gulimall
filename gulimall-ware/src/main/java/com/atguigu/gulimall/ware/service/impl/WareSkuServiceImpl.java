package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.OrderVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    WareOrderTaskService wareOrderTaskService;
    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    OrderFeignService orderFeignService;

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
     * 防止订单服务卡顿导致订单状态消息一直改不了，库存消息优先到期，查订单状态为新建，就什么都不做就丢掉了，导致卡顿的订单永远不能解锁
     * @param orderTo
     */
    @Override
    @Transactional
    public void unlockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        //查询订单状态
        R r = orderFeignService.getOrderStatus(orderSn);
        //此处不需要查订单状态了
        //需要查一下库存最新的解锁状态，防止重复解锁库存
        WareOrderTaskEntity task=wareOrderTaskService.getOderTaskByOrderSn(orderSn);
        Long id = task.getId();
        //按照库存工作单id找到所有没解锁状态的库存进行解锁
        List<WareOrderTaskDetailEntity> entities = wareOrderTaskDetailService.list(new LambdaQueryWrapper<WareOrderTaskDetailEntity>().eq(WareOrderTaskDetailEntity::getTaskId, id).eq(WareOrderTaskDetailEntity::getLockStatus, 1));
        for (WareOrderTaskDetailEntity entity : entities) {
            //此处注意有并发问题，和自动解锁的并发，可以自定义sql解决
            unLockStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum(),entity.getId());
        }
    }

    /**
     * 默认只要是运行时异常都会回滚
     * @param
     * @return
     */
    @Override
    public void unlockStock(StockLockedTo to) {
//        try {
            System.out.println("收到解锁库存的消息,库存工作单id：" + to.getId());
            Long id = to.getId();//库存工作单id
            StockDetailTo detail = to.getDetail();
            Long skuId = detail.getSkuId();
            //解锁
            Long detailId = detail.getId();
            //1.查询数据库关于这个id的锁定库存信息
            WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detailId);
            if (byId != null) {
                //有：可能需要解锁：1.没有对应订单，必须解锁2.有订单：a.订单已取消需要解锁，b.未取消，不解锁
                WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
                String orderSn = taskEntity.getOrderSn();
                //查询订单状态
                R r = orderFeignService.getOrderStatus(orderSn);
                if(r.getCode()==0){
                    //订单数据返回成功
                    OrderVo data = r.getData(new TypeReference<OrderVo>() {
                    });
                        //订单已经被取消，才能正确解锁
                        if(byId.getLockStatus()==1){
                            //当前工作单状态为锁定状态才需要解锁
                        unLockStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum(),detailId);
                        }
//                        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
                    }
                }else {
                    throw new RuntimeException("需要重新入队，抛异常？");
                    //消息拒绝以后重新放到队列，让别人继续消费解锁
//                    channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
                }

//            } else {
//                //没有：库存锁定失败，库存已经回滚，这种情况无需解锁
////                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
//            }
        }
//        catch (Exception e) {
//            System.out.println("错误..."+e.getMessage());
//            //可靠消息？
//            //此处拒绝还有异常怎么处理？
//            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
//
//        }
//    }

    /**
     * 库存解锁的场景
     * 1.下单成功，但订单过期自动取消或手动取消，都要解锁
     * 2.下订单成功，库存锁定成功，但接下来的业务调用失败，
     * 导致订单回滚，之前锁定的库存就要解锁
     *
     * @param vo
     * @return
     */
    @Override
    @Transactional(rollbackFor = NoStockException.class)
    public Boolean orderLockStock(WareSkuLockVo vo) {
        /**
         * 保存库存工作单的详情
         * 为了追溯库存锁定情况
         */
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(taskEntity);
        //1.找到每个商品在哪个仓库都有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            //查询这个商品在哪里有库存，并且应该全部锁成功后再发送消息到mq，避免取到没必要的消息
            QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).apply("(stock-stock_locked)>0").select("ware_id");//todo 散库存的情况
            List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(wrapper);
            List<Long> wareIds = wareSkuEntities.stream().map(WareSkuEntity::getWareId).collect(Collectors.toList());
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());
        for (SkuWareHasStock hasStock : collect) {
            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                //没有仓库有这个商品的库存
                //其他商品都不用扣了
                throw new NoStockException(skuId);
            }
            //1.如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发送给mq
            //2.后面出现某个商品锁定失败的情况，前面保存的工作单信息就回滚了，发送出去的消息，由于查不到id，所有就不用解锁

            for (Long wareId : wareIds) {
                //成功返回1，否则为0
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 0) {
                    //当前仓库锁失败，重试下一个仓库
                } else {
                    //成功
                    skuStocked = true;
                    //todo 告诉mq库存锁定成功
                    WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity(null, skuId, "", hasStock.getNum(), taskEntity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(entity);
                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(taskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(entity, stockDetailTo);
                    //只发id应该可以，回滚会全部回滚
                    lockedTo.setDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);
                    break;
                }
            }
            if (!skuStocked) {
                //所有仓库都没锁住
                throw new NoStockException(skuId);
            }
        }
        //2.锁定库存
        return true;
    }

    /**
     * 库存自动解锁：
     * 1.下订单成功，库存锁定成功，但接下来的业务调用失败，导致订单回滚，之前锁定的库存就要解锁
     * 2.下单成功，但订单过期自动取消或手动取消，都要解锁
     *
     * 只要解锁库存的消息失败，一定要告诉服务器此次解锁失败。--》手动ack
     */

    private void unLockStock(Long skuId,Long wareId,Integer num,Long taskDetailId){
        wareSkuDao.unLockStock(skuId,wareId,num);
        //更新库存工作单状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(entity);
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