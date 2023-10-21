package com.zzclearning.gulimall.product.service.impl;

import com.zzclearning.gulimall.product.dao.AttrDao;
import com.zzclearning.gulimall.product.vo.Catelog2Vo;
import com.zzclearning.gulimall.product.vo.Catelog3Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.Query;

import com.zzclearning.gulimall.product.dao.CategoryDao;
import com.zzclearning.gulimall.product.entity.CategoryEntity;
import com.zzclearning.gulimall.product.service.CategoryService;
import rx.internal.util.atomic.LinkedQueueNode;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    private CategoryDao categoryDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> getTreeMenus() {
        List<CategoryEntity> allEntities = baseMapper.selectList(null);
        //先获取所有根结点
        return allEntities.stream().filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                //.map(item -> {
                //    item.setChildren(getChildren(item, allEntities));
                //    return item;
                //})
                .peek(item -> item.setChildren(getChildren(item, allEntities)))
                .sorted(Comparator.comparingInt(CategoryEntity::getSort))
                .collect(Collectors.toList());
    }

    @Override
    public Long[] getCategoryPath(Long catelogId) {

        ArrayList<Long> cateIds = new ArrayList<>();
        CategoryEntity category;
        do {
            cateIds.add(catelogId);
            category = this.getById(catelogId);
            catelogId = category.getParentCid();
        } while (catelogId != 0);
        Collections.reverse(cateIds);
        return cateIds.toArray(new Long[0]);
    }

    @Override
    public String getCategoryName(Long catelogId) {
        List<String> stringList = new ArrayList<>();
        CategoryEntity category;
        do {
            category = this.getById(catelogId);
            if (category==null) {
                return "---该分类已删除";
            }
            stringList.add(category.getName());
            catelogId = category.getParentCid();
        } while (catelogId != 0);
        Collections.reverse(stringList);
        String categoryNames = String.join("/",stringList);
        return categoryNames;

    }

    /**
     * 自定义Cache：1.指定缓存的key；
     * 2.指定缓存的过期时间；
     * 3.将数据以json格式保存
     * @return
     */
    @Override
    @Cacheable(value = "category",key = "#root.methodName")//默认将缓存分区名字作为key的前缀；方法名作为key-->category::getLevel1Categories
    public List<CategoryEntity> getLevel1Categories() {
        System.out.println("getLevel1Categories---查询一级分类");
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid",0));
    }

    @Override
    public List<CategoryEntity> getLevel2Categories(Long cat1Id) {
        return categoryDao.getLevel2Categories(cat1Id);
    }

    @Override
    public List<CategoryEntity> getLevel3Categories(Long cat2Id) {
        return categoryDao.getLevel3Categories(cat2Id);
    }

    private List<CategoryEntity> getCategoriesByPid(List<CategoryEntity> categoryEntities,Long pid) {
        return categoryEntities.stream().filter(item-> pid.equals(item.getParentCid())).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "category", key = "#root.methodName")
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        return getCatelogJsonFromDb();
        //System.out.println("空值...");
        //return null;
    }

    //从数据库查询并封装分类数据
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDb() {
        System.out.println("getCatelogJsonFromDb--查询数据库");
        //优化：只查询一次数据库获取全部分类信息
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        Map<String,List<Catelog2Vo>> map = new HashMap<>();
        //1.获取所有一级分类id
        List<CategoryEntity> level1Categories = this.getCategoriesByPid(categoryEntities,0L);
        List<Long> cat1Ids = level1Categories.stream().map(CategoryEntity::getCatId).collect(Collectors.toList());
        //2.组装二级分类集合
        for (Long cat1Id : cat1Ids) {
            //二级分类vo
            List<CategoryEntity> level2Categories = this.getCategoriesByPid(categoryEntities,cat1Id);
            if (level2Categories != null) {
                List<Catelog2Vo> catelog2Vos = level2Categories.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo();
                    catelog2Vo.setCatelog1Id(cat1Id.toString());
                    catelog2Vo.setId(l2.getCatId().toString());
                    catelog2Vo.setName(l2.getName());
                    //三级分类vo
                    List<CategoryEntity> level3Categories =this.getCategoriesByPid(categoryEntities,l2.getCatId());
                    if (level3Categories != null) {
                        List<Catelog3Vo> catelog3Vos = level3Categories.stream().map(l3 -> {
                            Catelog3Vo catelog3Vo = new Catelog3Vo();
                            catelog3Vo.setCatelog2Id(l2.getCatId().toString());
                            catelog3Vo.setId(l3.getCatId().toString());
                            catelog3Vo.setName(l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatelog3Vos(catelog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
                map.put(cat1Id.toString(),catelog2Vos);
            }
        }
        return map;
    }

    public List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream().filter(item -> item.getParentCid().equals(root.getCatId()) )
                    .peek(item -> item.setChildren(getChildren(item, all)))
                    //        .map(item -> {
                    //            item.setChildren(getChildren(item, all));
                    //            return item;
                    //        })
                // 最开始getSort== null,使用comparingInt方法，会将Integer 自动拆箱，Integer x = categoryEntity.getSort()-->x.intValue-->NullPointerException
                    .sorted(Comparator.comparingInt(CategoryEntity::getSort))
                    .collect(Collectors.toList());
    }

}