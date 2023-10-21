package com.zzclearning.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzclearning.gulimall.product.entity.AttrGroupEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.Query;

import com.zzclearning.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.zzclearning.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.zzclearning.gulimall.product.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
@Slf4j
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {
    @Autowired
    private AttrAttrgroupRelationDao dao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<AttrAttrgroupRelationEntity> list(Long attrGroupId) {
        LambdaQueryWrapper<AttrAttrgroupRelationEntity> wrapper = Wrappers.lambdaQuery();
        return this.list(wrapper.eq(AttrAttrgroupRelationEntity::getAttrGroupId,attrGroupId));

    }

    @Override
    public int removeBatch(List<AttrAttrgroupRelationEntity> entities) {
        return dao.removeBatch(entities);
    }

    @Override
    public AttrAttrgroupRelationEntity getByAttrId(Long attrId) {
        return this.getOne(new LambdaQueryWrapper<>(AttrAttrgroupRelationEntity.class).eq(AttrAttrgroupRelationEntity::getAttrId,attrId));
    }


}