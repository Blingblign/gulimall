package com.zzclearning.gulimall.product.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzclearning.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.zzclearning.gulimall.product.entity.AttrEntity;
import com.zzclearning.gulimall.product.service.AttrAttrgroupRelationService;
import com.zzclearning.gulimall.product.service.AttrService;
import com.zzclearning.gulimall.product.service.CategoryService;
import com.zzclearning.gulimall.product.vo.AttrGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zzclearning.gulimall.product.entity.AttrGroupEntity;
import com.zzclearning.gulimall.product.service.AttrGroupService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.R;



/**
 * 属性分组
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 16:44:33
 */
@Slf4j
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    /**
     * 列表
     */
    @RequestMapping("/list/{categoryId}")
        public R list(@RequestParam Map<String, Object> params, @PathVariable("categoryId") Long categoryId){
        //PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params,categoryId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
        public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] categoryPath = categoryService.getCategoryPath(catelogId);
        attrGroup.setCatelogPath(categoryPath);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
        public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }
    /**
     * 保存
     */
    @RequestMapping("/batchSave")
    public R batchSave(@RequestBody List<AttrGroupVo> attrGroupVos){
        try {
            attrGroupService.batchSave(attrGroupVos);
        } catch (Exception e) {
            log.error(e.getMessage());
            return R.error(e.getMessage());
        }
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
        public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
        public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));
        //级联删除属性-分组表
        attrAttrgroupRelationService.remove(new LambdaQueryWrapper<>(AttrAttrgroupRelationEntity.class).in(AttrAttrgroupRelationEntity::getAttrGroupId,Arrays.asList(attrGroupIds)));
        return R.ok();
    }

    /**
     * 查询当前分组的所有属性
     */
    @GetMapping("/{attrGroupId}/attr/relation")
    public R getAttrRelation(@PathVariable("attrGroupId") Long attrGroupId) {
        List<AttrAttrgroupRelationEntity> entities = attrAttrgroupRelationService.list(attrGroupId);
        log.info("分组id为{}，关联属性的个数为{}",attrGroupId,entities.size());
        List<Long> attrIds = entities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        //TODO 属性为空会被删除吗？
        log.warn("分组的属性id为：{}",attrIds);
        List<AttrEntity> attrEntities = null;
        if (attrIds.size() != 0) {
            attrEntities = attrService.listByIds(attrIds);
        }
        return R.ok().put("data",attrEntities);
    }

    /**
     *删除属性与分组的关联关系
     * TODO 使用vo对象接受attrId和attrGroupID
     */
    @PostMapping("/attr/relation/delete")
    public R removeAttrRelation(@RequestBody List<AttrAttrgroupRelationEntity> entities) {
        int result = attrAttrgroupRelationService.removeBatch(entities);
        return result > 0 ? R.ok() : R.error();
    }

    /**
     *获取属性分组没有关联的其他属性
     * 不能为销售属性
     * 必须在该分组的分类下
     * 不能被其他分组关联
     */
    @GetMapping("/{attrGroupId}/noattr/relation")
    public R getNoAttrRelation(@RequestParam Map<String, Object> params, @PathVariable("attrGroupId") Long attrGroupId) {
        PageUtils page = attrService.queryNoRelationAttr(params, attrGroupId);
        return R.ok().put("page",page);
    }

    /**
     * 添加属性与分组关联关系
     */
    @PostMapping("/attr/relation")
    public R addAttrRelation(@RequestBody List<AttrAttrgroupRelationEntity> entities) {
        boolean result = attrAttrgroupRelationService.saveBatch(entities);

        return result? R.ok() : R.error();
    }
    /**
     * 获取分类下所有分组&关联属性
     */
    @GetMapping("/{catelogId}/withattr")
    public R getGroupsWithAttrs(@PathVariable("catelogId") Long catelogId) {
        List<AttrGroupVo> attrGroupVos = attrGroupService.getGroupsWithAttrs(catelogId);
        return R.ok().put("data",attrGroupVos);
    }
}
