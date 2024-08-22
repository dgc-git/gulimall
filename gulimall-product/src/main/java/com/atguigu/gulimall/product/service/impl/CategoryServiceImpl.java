package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redisson;
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(new Query<CategoryEntity>().getPage(params), new QueryWrapper<>());

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2.组装成父子的树形结构
        //2.1找到一级分类
        return entities.stream().filter(Objects::nonNull).filter((categoryEntity -> categoryEntity.getParentCid() == 0)).peek((menu) -> menu.setChildren(getChildrens(menu, entities))).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))).collect(Collectors.toList());
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //todo 1.检查当前删除的菜单，是否在别的地方被引用
        baseMapper.deleteBatchIds(asList);
    }

    //todo 产生堆外内存溢出outOfDirectMemoryError
    //springboot2.0以后默认使用lettuce作为redis客户端，它使用netty进行网络通信
    //lettuce的bug导致内存溢出，netty如果没有指定堆外内存，默认使用-Xmx100m
    //不能只通过-Dio.netty.maxDirectMemory调整，可以升级lettuce客户端或者切换jedis
//    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson2() {
        /**
         * 1.空结果缓存：缓存穿透
         * 2.设置过期时间+随机值：缓存雪崩
         * 3.加锁：解决缓存击穿
         */
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            return getCatalogJsonFromDbWithRedissonLock();
        }
        return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
    }

    @Override
    @Cacheable(value = "category",key = "#root.methodName",sync = true)
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        /**
         * 将多次查库变为一次
         */
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        //查出所有1级分类
        List<CategoryEntity> level1Categorys = getParentCid(selectList, 0L);
        //封装数据
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //每一个一级分类，查到这个一级分类的所有二级分类
            List<CategoryEntity> categoryEntities = getParentCid(selectList, v.getCatId());
            //2.封装上面的结果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //找当前2级分类的三级分类，封装成vo
                    List<CategoryEntity> level3Catelog = getParentCid(selectList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.catelog3Vo> collect = level3Catelog.stream().map(l3 -> {
                            //封装成指定格式
                            Catelog2Vo.catelog3Vo catelog3Vo = new Catelog2Vo.catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;

                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return parent_cid;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {
        //分布式锁
        RLock lock = redisson.getLock("catalogJson-lock");
        lock.lock(10,TimeUnit.SECONDS);
        //只能删除自己的锁
        //设置过期时间，防止删不掉锁
            System.out.println("获取分布式锁成功");
            Map<String, List<Catelog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                lock.unlock();
            }
            return dataFromDb;



    }
//    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDb() {
//        //分布式锁
//        //只能删除自己的锁
//        String uuid = UUID.randomUUID().toString();
//        //设置过期时间，防止删不掉锁
//        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
//        if (lock) {
//            System.out.println("获取分布式锁成功");
//            Map<String, List<Catelog2Vo>> dataFromDb;
//            try {
//                dataFromDb = getDataFromDb();
//            } finally {
//                String luaScript = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
//                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(luaScript, Long.class), Arrays.asList("lock"), uuid);
//            }
////            redisTemplate.delete("lock");
////            String lockValue = redisTemplate.opsForValue().get("lock");
////            if(uuid.equalsIgnoreCase(lockValue)){
////                //删除自己的锁
////
////            }
//            //lua脚本
//
//            return dataFromDb;
//
//        } else {
//            //休眠100ms重试
//            System.out.println("获取分布式锁失败，等待重试");
//            try {
//                Thread.sleep(200);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
//            if (!StringUtils.isEmpty(catalogJSON)) {
//                return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
//            }
//            return getCatalogJsonFromDb();
//        }
//
//
//    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        //得到锁以后再去缓存中确定一次，如果没有才需要继续查询
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            System.out.println("查询了数据库");
            /**
             * 将多次查库变为一次
             */
            List<CategoryEntity> selectList = baseMapper.selectList(null);

            //查出所有1级分类
            List<CategoryEntity> level1Categorys = getParentCid(selectList, 0L);
            //封装数据
            Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                //每一个一级分类，查到这个一级分类的所有二级分类
                List<CategoryEntity> categoryEntities = getParentCid(selectList, v.getCatId());
                //2.封装上面的结果
                List<Catelog2Vo> catelog2Vos = null;
                if (categoryEntities != null) {
                    catelog2Vos = categoryEntities.stream().map(l2 -> {
                        Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                        //找当前2级分类的三级分类，封装成vo
                        List<CategoryEntity> level3Catelog = getParentCid(selectList, l2.getCatId());
                        if (level3Catelog != null) {
                            List<Catelog2Vo.catelog3Vo> collect = level3Catelog.stream().map(l3 -> {
                                //封装成指定格式
                                Catelog2Vo.catelog3Vo catelog3Vo = new Catelog2Vo.catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                                return catelog3Vo;

                            }).collect(Collectors.toList());
                            catelog2Vo.setCatalog3List(collect);
                        }
                        return catelog2Vo;
                    }).collect(Collectors.toList());
                }
                return catelog2Vos;
            }));
            String jsonString = JSON.toJSONString(parent_cid);
            redisTemplate.opsForValue().set("catalogJSON", jsonString, 1, TimeUnit.DAYS);
            return parent_cid;
        } else {
            return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
        }
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> selectList, Long parentCid) {
//        return baseMapper.selectList(new LambdaQueryWrapper<CategoryEntity>().eq(CategoryEntity::getParentCid, v.getCatId()));
        return selectList.stream().filter(item -> Objects.equals(item.getParentCid(), parentCid)).collect(Collectors.toList());
    }

    @Override
    //每一个需要缓存的数据我们都需要指定要放到哪个名字的缓存中，进行分区
    @Cacheable(value = {"category"},key = "#root.methodName")//当前方法结果需要缓存，如果缓存中有，方法不用调用，如果缓存中没有，会调用方法，并将方法的结果放入缓存
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("方法调用了，从数据库查咯...");
        return baseMapper.selectList(new LambdaQueryWrapper<CategoryEntity>().eq(CategoryEntity::getParentCid, 0));
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        return parentPath.toArray(new Long[0]);
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        //收集当前节点id
        paths.add(catelogId);
        return paths;
    }

    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream().filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId())).peek(categoryEntity -> categoryEntity.setChildren(getChildrens(categoryEntity, all))).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))).collect(Collectors.toList());
    }
    /**
     * 级联更新所有关联表的冗余数据
     * 缓存策略：失效模式，方法执行完删除缓存
     */
    @CacheEvict(value = {"category"}, allEntries = true)//失效模式
    //存储同一类型的数据都i可以指定成同一个分区
//    @Caching(evict = {@CacheEvict(value = {"category"}, key = "'getLevel1Categorys'"),
//            @CacheEvict(value = {"category"}, key = "'getCatalogJson'")})
    //@CachePut //双写模式，如果返回值正好是最新的数据就可以使用
    /**
     * 1.读模式：穿透：缓存空数据，击穿：cache框架是否加锁？雪崩：加随机时间
     * 2.写模式：保证一致性问题：读写加锁（适用于都多写少的系统）；引入canal，感知mysql的更新去更新缓存；读多写多的情况直接去数据库查询
     *
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            // 更新冗余表
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
            // TODO 更新其他冗余表
        }
    }

}