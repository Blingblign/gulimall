package com.zzclearning.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzclearning.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.zzclearning.gulimall.product.entity.AttrEntity;
import com.zzclearning.gulimall.product.service.AttrAttrgroupRelationService;
import com.zzclearning.gulimall.product.service.AttrService;
import com.zzclearning.gulimall.product.vo.AttrGroupVo;
import com.zzclearning.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.Query;

import com.zzclearning.gulimall.product.dao.AttrGroupDao;
import com.zzclearning.gulimall.product.entity.AttrGroupEntity;
import com.zzclearning.gulimall.product.service.AttrGroupService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;
    @Autowired
    AttrService attrService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long categoryId) {
        LambdaQueryWrapper<AttrGroupEntity> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != 0) {
            wrapper.eq(AttrGroupEntity::getCatelogId, categoryId);
        }
        Object key = params.get("key");//关键字
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((item)->{
                item.eq(AttrGroupEntity::getAttrGroupId,key).or().like(AttrGroupEntity::getAttrGroupName, key).or().like(AttrGroupEntity::getDescript, key);
            });
        }
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
        return new PageUtils(page);

    }

    @Override
    public List<AttrGroupVo> getGroupsWithAttrs(Long catelogId) {
        List<AttrGroupEntity> attrGroupEntities = this.list(Wrappers.lambdaQuery(AttrGroupEntity.class).eq(AttrGroupEntity::getCatelogId, catelogId));
        return attrGroupEntities.stream().map(item->{
            AttrGroupVo attrGroupVo = new AttrGroupVo();
            BeanUtils.copyProperties(item,attrGroupVo);
            //查询该分组下的所有属性
            List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = attrAttrgroupRelationService.list(Wrappers.lambdaQuery(
                    AttrAttrgroupRelationEntity.class).eq(AttrAttrgroupRelationEntity::getAttrGroupId, item.getAttrGroupId()));
            List<Long> attrIds = attrAttrgroupRelationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
            List<AttrEntity> attrEntities = null;
            if (attrIds.size() != 0) {
                attrEntities = attrService.listByIds(attrIds);
            }
            attrGroupVo.setAttrs(attrEntities);
            return attrGroupVo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SkuItemVo.AttrGroupWithAttrsVo> getAttrGroupWithAttrs(Long spuId) {
        return baseMapper.getAttrGroupWithAttrs(spuId);
    }

    @Override
    @Transactional
    public void batchSave(List<AttrGroupVo> attrGroupVos) {
        //判断是否已经批量添加过
        AttrGroupEntity one = this.getOne(Wrappers.lambdaQuery(AttrGroupEntity.class).eq(AttrGroupEntity::getAttrGroupName, attrGroupVos.get(0).getAttrGroupName()));
        if (one != null) {
            throw new RuntimeException("请勿重复添加");
        }
        //第一次添加,
        //添加属性分组信息
        List<AttrGroupEntity> groupEntities = attrGroupVos.stream().map(attrGroupVo -> {
            AttrGroupEntity attrGroupEntity = new AttrGroupEntity();
            BeanUtils.copyProperties(attrGroupVo, attrGroupEntity);
            return attrGroupEntity;
        }).collect(Collectors.toList());
        this.saveBatch(groupEntities);

        //添加属性信息
        List<AttrEntity> attrEntities = new ArrayList<>();
        attrGroupVos.forEach(attrGroupVo -> {
            attrEntities.addAll(attrGroupVo.getAttrs());
        });
        attrService.saveBatch(attrEntities);
        ArrayList<AttrAttrgroupRelationEntity> attrgroupRelationEntities = new ArrayList<>();
        //添加属性--分组关系
        for (int i = 0; i < attrGroupVos.size(); i++) {
            Long groupId = groupEntities.get(i).getAttrGroupId();
            List<AttrAttrgroupRelationEntity> relationEntities = attrGroupVos.get(i).getAttrs().stream().map(attrEntity -> {
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
                attrAttrgroupRelationEntity.setAttrGroupId(groupId);
                attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
                return attrAttrgroupRelationEntity;
            }).collect(Collectors.toList());
            attrgroupRelationEntities.addAll(relationEntities);
        }
        attrAttrgroupRelationService.saveBatch(attrgroupRelationEntities);
    }

}