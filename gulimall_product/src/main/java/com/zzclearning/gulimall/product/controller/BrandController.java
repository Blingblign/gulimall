package com.zzclearning.gulimall.product.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

////import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.zzclearning.common.validator.group.AddGroup;
import com.zzclearning.common.validator.group.UpdateGroup;
import com.zzclearning.common.validator.group.UpdateStatusGroup;
import com.zzclearning.gulimall.product.service.CategoryBrandRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zzclearning.gulimall.product.entity.BrandEntity;
import com.zzclearning.gulimall.product.service.BrandService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.R;

import javax.validation.Valid;
import javax.validation.valueextraction.ExtractedValue;


/**
 * 品牌
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 16:44:33
 */
@RestController
@Slf4j
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 列表
     */
    @RequestMapping("/list")
        public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
        public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     * ‘@Validated’ 进行统一的分组校验
     */
    @RequestMapping("/save")
        public R save(@Validated(AddGroup.class) @RequestBody BrandEntity brand){

		brandService.save(brand);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
        public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand){
		brandService.updateById(brand);

        return R.ok();
    }
    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand){
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除(级联删除品牌分类关系表)
     */
    @RequestMapping("/delete")
        public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));
        categoryBrandRelationService.remove(brandIds,BrandEntity.class);
        return R.ok();
    }
    /**
     *
     */
    /**
     * 保存
     * ‘@Validated’ 进行统一的分组校验
     */
    @RequestMapping("/batchSave")
    public R saveAll(@Validated(AddGroup.class) @RequestBody List<BrandEntity> brands){
        try {
            boolean isSuccess = brandService.batchSave(brands,null);
            if (isSuccess) return R.ok();
        } catch (Exception e) {
            log.error("批量添加品牌出错，错误信息：{}",e.getMessage());
            return R.error(e.getMessage());
        }
        return R.error();
    }

}
