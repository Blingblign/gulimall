package com.zzclearning.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.zzclearning.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zzclearning.gulimall.product.entity.CategoryEntity;
import com.zzclearning.gulimall.product.service.CategoryService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.R;



/**
 * 商品三级分类
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 16:44:33
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 列表
     */
    @RequestMapping("/list")
        public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 获取分类多级菜单
     */
    @RequestMapping("/list/tree")
    public R getTreeMenus(){
        List<CategoryEntity> data = categoryService.getTreeMenus();
        return R.ok().put("data", data);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
        public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
        public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
        public R update(@RequestBody CategoryEntity category){
		categoryService.updateById(category);
        //TODO 修改品牌分类关系表

        return R.ok();
    }
    @RequestMapping("/update/sort")
    public R update(@RequestBody List<CategoryEntity> categories){
        categoryService.saveOrUpdateBatch(categories);

        return R.ok();
    }

    /**
     * 删除(级联删除品牌分类关系表)
     */
    @RequestMapping("/delete")
        public R delete(@RequestBody Long[] catIds){
		categoryService.removeByIds(Arrays.asList(catIds));
        categoryBrandRelationService.remove(catIds,CategoryEntity.class);
        return R.ok();
    }

}
