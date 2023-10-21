package com.zzclearning.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzclearning.gulimall.product.service.BrandService;
import com.zzclearning.gulimall.product.service.CategoryService;
import com.zzclearning.gulimall.product.vo.BrandVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zzclearning.gulimall.product.entity.CategoryBrandRelationEntity;
import com.zzclearning.gulimall.product.service.CategoryBrandRelationService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.R;



/**
 * 品牌分类关联
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 16:44:33
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;

    /**
     * Controller:处理请求，接收和校验数据
     * Service接受Controller传来的数据，进行业务处理
     *Controller接收Service处理好的数据，封装成页面指定的vo对象
     * 获取分类的品牌列表
     */
    @GetMapping("/brands/list")
        public R listBrands(@RequestParam("catId") Long catelogId){
        List<CategoryBrandRelationEntity> brandList = categoryBrandRelationService.getBrandsByCatelogId(catelogId);
        List<BrandVo> brandVos = brandList.stream().map(item-> {
            BrandVo brandVo = new BrandVo();
            BeanUtils.copyProperties(item,brandVo);
            return brandVo;
        }).collect(Collectors.toList());
        return R.ok().put("data", brandVos);
    }
    /**
     * 获取品牌的分类列表
     */
    @GetMapping("/catelog/list")
    public R categoryList(@RequestParam("brandId") Long brandId){
        List<CategoryBrandRelationEntity> list = categoryBrandRelationService.getCategoryList(brandId);
        return R.ok().put("data", list);
    }
    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
        public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        String brandName = brandService.getById(brandId).getName();
        String categoryName = categoryService.getById(catelogId).getName();
        categoryBrandRelation.setBrandName(brandName);
        categoryBrandRelation.setCatelogName(categoryName);
        categoryBrandRelationService.save(categoryBrandRelation);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
        public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
        public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
