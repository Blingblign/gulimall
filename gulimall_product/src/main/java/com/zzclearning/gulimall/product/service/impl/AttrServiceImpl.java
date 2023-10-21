package com.zzclearning.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzclearning.gulimall.product.constant.ProductConstant;
import com.zzclearning.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.zzclearning.gulimall.product.dao.AttrGroupDao;
import com.zzclearning.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.zzclearning.gulimall.product.entity.AttrGroupEntity;
import com.zzclearning.gulimall.product.entity.ProductAttrValueEntity;
import com.zzclearning.gulimall.product.service.CategoryService;
import com.zzclearning.gulimall.product.service.ProductAttrValueService;
import com.zzclearning.gulimall.product.vo.AttrRepVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.Query;

import com.zzclearning.gulimall.product.dao.AttrDao;
import com.zzclearning.gulimall.product.entity.AttrEntity;
import com.zzclearning.gulimall.product.service.AttrService;

@Slf4j
@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    private AttrDao attrDao;
    @Autowired
    private AttrGroupDao attrGroupDao;
    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId, String attrType) {
        LambdaQueryWrapper<AttrEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(catelogId != 0,AttrEntity::getCatelogId,catelogId)
                //动态获取属性类型：基本or销售属性
                .eq(AttrEntity::getAttrType,"base".equalsIgnoreCase(attrType) ? ProductConstant.AttrType.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrType.ATTR_TYPE_SALE.getCode());
        //关键字
        keySelect(wrapper, (String) params.get("key"));
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        PageUtils pageUtils = new PageUtils(page);
        //组装vo对象
        List<Object> attrRepVoList = page.getRecords().stream().map(attrEntity -> {
            AttrRepVo attrRepVo = new AttrRepVo();
            BeanUtils.copyProperties(attrEntity, attrRepVo);
            //三级分类名
            String categoryName;
            if (catelogId != 0) {
                categoryName = categoryService.getCategoryName(catelogId);
            } else {
                categoryName = categoryService.getCategoryName(attrEntity.getCatelogId());
            }
            attrRepVo.setCatelogName(categoryName);
            //属性分组名
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(Wrappers.lambdaQuery(AttrAttrgroupRelationEntity.class).eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId()));
            if (attrAttrgroupRelationEntity == null) {
                attrRepVo.setGroupName(null);
                log.error("属性id:{}未关联分组数据",attrEntity.getAttrId());
            } else {
                Long attrGroupId = attrAttrgroupRelationEntity.getAttrGroupId();
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
                //Assert.notNull(attrGroupEntity,"属性对应的分组不存在");
                if (attrGroupEntity == null) {
                    log.error("属性id:{}关联表数据未删除，分组不存在",attrEntity.getAttrId());
                } else{
                    attrRepVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            return attrRepVo;
        }).collect(Collectors.toList());
        pageUtils.setList(attrRepVoList);
        return pageUtils;
    }



    @Override
    public PageUtils queryPage(Map<String, Object> params, List<Long> attrIds) {
        LambdaQueryWrapper<AttrEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.notIn(attrIds.size()!=0,AttrEntity::getAttrId, attrIds)
                //排除销售属性
                .eq(AttrEntity::getAttrType, ProductConstant.AttrType.ATTR_TYPE_SALE.getCode());
        keySelect(wrapper, (String) params.get("key"));
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryNoRelationAttr(Map<String, Object> params, Long attrGroupId) {
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //查询该分类下所有分组id
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(Wrappers.lambdaQuery(AttrGroupEntity.class).eq(AttrGroupEntity::getCatelogId, catelogId));
        List<Long> attrGroupIds = attrGroupEntities.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
        //查询已关联的所有属性id
        List<AttrAttrgroupRelationEntity> entities = attrAttrgroupRelationDao.selectList(Wrappers.lambdaQuery(AttrAttrgroupRelationEntity.class).
                in(AttrAttrgroupRelationEntity::getAttrGroupId, attrGroupIds));
        List<Long> attrIds = entities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        log.info("已关联的所有属性id:{}",attrIds);
        //查询该分组所在分类的所有未关联的基本属性
        LambdaQueryWrapper<AttrEntity> wrapper = Wrappers.lambdaQuery(AttrEntity.class)
                .eq(AttrEntity::getCatelogId, catelogId)
                .eq(AttrEntity::getAttrType, ProductConstant.AttrType.ATTR_TYPE_BASE.getCode())
                .notIn(AttrEntity::getAttrId, attrIds);
        //关键字搜索
        keySelect(wrapper,(String) params.get("key"));
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params), wrapper);
        return new PageUtils(page);

    }

    @Override
    public List<ProductAttrValueEntity> listAttrForSpu(Long spuId) {
        LambdaQueryWrapper<ProductAttrValueEntity> wrapper = Wrappers.lambdaQuery(ProductAttrValueEntity.class).eq(ProductAttrValueEntity::getSpuId, spuId);
        return productAttrValueService.list(wrapper);
    }

    /**
     * 属性关键词检索
     * @param wrapper
     * @param key
     * @return
     */
    private void  keySelect(LambdaQueryWrapper<AttrEntity> wrapper,String key) {
        if (!StringUtils.isBlank(key)) {
            wrapper.and(item->{
                item.like(AttrEntity::getAttrId,key)
                        .or().like(AttrEntity::getAttrName,key)
                        //.or().like(AttrEntity::getIcon,key)
                        .or().like(AttrEntity::getValueSelect,key);

            });
        }
    }

}