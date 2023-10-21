package com.zzclearning.gulimall.product.controller;

import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zzclearning.gulimall.product.constant.ProductConstant;
import com.zzclearning.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.zzclearning.gulimall.product.entity.ProductAttrValueEntity;
import com.zzclearning.gulimall.product.service.AttrAttrgroupRelationService;
import com.zzclearning.gulimall.product.service.AttrGroupService;
import com.zzclearning.gulimall.product.service.CategoryService;
import com.zzclearning.gulimall.product.vo.AttrVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zzclearning.gulimall.product.entity.AttrEntity;
import com.zzclearning.gulimall.product.service.AttrService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.R;



/**
 * 商品属性
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 16:44:33
 */
@Slf4j
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
        public R info(@PathVariable("attrId") Long attrId){
		AttrEntity attr = attrService.getById(attrId);
        //三级分类路径
        Long[] categoryPath = categoryService.getCategoryPath(attr.getCatelogId());
        //所属分组
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationService.getByAttrId(attrId);
        Long attrGroupId = null;
        if (attrAttrgroupRelationEntity != null) {
            attrGroupId = attrAttrgroupRelationEntity.getAttrGroupId();
        }
        //属性复制给vo对象
        AttrVo attrVo = new AttrVo();
        BeanUtils.copyProperties(attr,attrVo);
        attrVo.setCatelogPath(categoryPath);

        attrVo.setAttrGroupId(attrGroupId);

        return R.ok().put("attr", attrVo);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
        public R save(@RequestBody AttrVo attrVo){
        AttrEntity attr = new AttrEntity();
        BeanUtils.copyProperties(attrVo,attr);
        attrService.save(attr);
        //基本属性并且选择了分组才需要关联分组;
        if (attr.getAttrType() .equals(ProductConstant.AttrType.ATTR_TYPE_BASE.getCode()) && attrVo.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());
            log.info("新增属性id为：{}",attr.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
        }

        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
        public R update(@RequestBody AttrVo attrVo){
        AttrEntity attr = new AttrEntity();
        BeanUtils.copyProperties(attrVo,attr);
        attrService.updateById(attr);
        if (attr.getAttrType() .equals(ProductConstant.AttrType.ATTR_TYPE_BASE.getCode())) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrVo.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            //看属性-分组关系是否存在，存在则为修改，不存在则未新增关系
            int count = attrAttrgroupRelationService.count(new LambdaUpdateWrapper<>(AttrAttrgroupRelationEntity.class)
                    .eq(AttrAttrgroupRelationEntity::getAttrId, attrVo.getAttrId()));
            if (count > 0) {
                //修改
                attrAttrgroupRelationService.update(attrAttrgroupRelationEntity,new LambdaUpdateWrapper<>(AttrAttrgroupRelationEntity.class)
                        .eq(AttrAttrgroupRelationEntity::getAttrId,attrVo.getAttrId()));
            } else {
                //新增
                attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
            }
        }
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
        public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));
        //级联删除属性:属性-分组表
        attrAttrgroupRelationService.remove(new LambdaQueryWrapper<>(AttrAttrgroupRelationEntity.class)
                .in(AttrAttrgroupRelationEntity::getAttrId,Arrays.asList(attrIds)));
        return R.ok();
    }

    /**
     * 获取分类规格参数
     * /base/list/{catelogId}
     */
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseOrSaleList(@RequestParam Map<String, Object> params,@PathVariable("catelogId") Long catelogId,@PathVariable("attrType") String attrType) {
        //获得三级分类下的所有属性
        PageUtils page = attrService.queryPage(params,catelogId,attrType);
        return R.ok().put("page",page);
    }

    /**
     * 获取spu规格
     * /base/list/{catelogId}
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R listAttrForSpu(@PathVariable("spuId") Long spuId) {
        //获得三级分类下的所有属性
        List<ProductAttrValueEntity> entities = attrService.listAttrForSpu(spuId);
        return R.ok().put("data",entities);
    }

}
