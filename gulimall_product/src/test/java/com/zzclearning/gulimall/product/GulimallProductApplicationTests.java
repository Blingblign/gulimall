package com.zzclearning.gulimall.product;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.zzclearning.gulimall.product.dao.AttrGroupDao;
import com.zzclearning.gulimall.product.dao.SkuSaleAttrValueDao;
import com.zzclearning.gulimall.product.entity.BrandEntity;
import com.zzclearning.gulimall.product.entity.CategoryEntity;
import com.zzclearning.gulimall.product.service.BrandService;
import com.zzclearning.gulimall.product.service.CategoryService;
import com.zzclearning.gulimall.product.vo.SkuItemVo;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.ManagementServerPortUtils;
import org.springframework.test.context.junit4.SpringRunner;
import sun.security.krb5.internal.crypto.crc32;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallProductApplicationTests {
    @Autowired
    RedissonClient redisClient;

    @Resource
    BrandService brandService;
    @Resource
    BaseMapper<CategoryEntity> baseMapper;
    @Resource
    CategoryService categoryService;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void testAttrGroupWithAttrs() {

        List<SkuItemVo.AttrGroupWithAttrsVo> attrGroupWithAttrs = attrGroupDao.getAttrGroupWithAttrs(11l);
        System.out.println("分组属性---attrGroupWithAttrs："+ attrGroupWithAttrs);
        List<SkuItemVo.SaleAttrVo> skuItemSaleAttrs = skuSaleAttrValueDao.getSkuItemSaleAttrs(11l);
        System.out.println("销售属性---skuItemSaleAttrs："+ skuItemSaleAttrs);

    }
    @Test
    public void testSaveBatch() {
        List<BrandEntity> brandEntities = new ArrayList <>();
        brandService.saveBatch(brandEntities);
    }
    @Test
    public void testCatelogPath() {
        Long[] categoryPath = categoryService.getCategoryPath(178L);
        System.out.println(Arrays.asList(categoryPath));
    }
    @Test
    void contextLoads() {
        List<BrandEntity> list = brandService.list();
        for (BrandEntity brandEntity : list) {
            System.out.println(brandEntity);
        }

    }
    @Test
    public void testStream() {
        //List<CategoryEntity> list = baseMapper.selectList(null);
        //;
        //List<CategoryEntity> children = getChildren(list.get(0), list);
        //System.out.println(children);

    }
    @Test
    public void testRedisson() throws InterruptedException, IOException {
        RLock lock = redisClient.getLock("lock:order:" + "1623213124543");
        boolean b = lock.tryLock(10,10, TimeUnit.SECONDS);
        if (b) {
            System.out.println("testRedisson获得了锁" +LocalDateTime.now());
        }
        Thread.sleep(1000);
        Thread t2 = new Thread(() -> {
            boolean b2 = false;
            try {
                System.out.println("t2等待锁："+LocalDateTime.now());
                b2 = lock.tryLock(12, 10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (b2) {
                System.out.println("t2获得了锁" + LocalDateTime.now());
            } else {
                System.out.println(LocalDateTime.now());
                System.out.println("t2获取锁失败" + LocalDateTime.now());
            }
        },"t2");
        t2.start();
        Thread t3 = new Thread(() -> {
            try {
                Thread.sleep(11000);
                boolean b1 = lock.tryLock(10,10,TimeUnit.SECONDS);
                if (b1) System.out.println("t3获得锁" + LocalDateTime.now());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"t3");
        t3.start();

        System.in.read();


    }
    @Test
    public void testLocalDateTime() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
        System.out.println(now);
    }

}
