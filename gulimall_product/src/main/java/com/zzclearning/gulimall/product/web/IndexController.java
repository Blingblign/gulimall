package com.zzclearning.gulimall.product.web;

import com.zzclearning.gulimall.product.entity.CategoryEntity;
import com.zzclearning.gulimall.product.service.CategoryService;
import com.zzclearning.gulimall.product.vo.Catelog2Vo;
import com.zzclearning.gulimall.product.vo.Catelog3Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author bling
 * @create 2023-01-31 11:17
 */
@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;

    /**
     * 查询所有一级分类
     * @param model
     * @return
     */
    @GetMapping({"/","index.html"})
    public String indexPage(Model model) {
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categories();
        model.addAttribute("categorys",categoryEntities);
        return "index";
    }

    /**
     * 查询三级分类
     * {
     *     "一级分类ID": [
     *         {
     *             "catalog1Id": "一级分类ID",
     *             "id": "二级分类ID",
     *             "name": "二级分类名",
     *             "catalog3List":[
     *                 {
     *                     "catalog2Id": "二级分类ID",
     *                     "id": "三级分类ID",
     *                     "name": "三级分类名"
     *                 }
     *             ]
     *         }
     *     ],
     *     "一级分类ID": [],
     *     "一级分类ID": []
     * }
     */
    @ResponseBody
    @GetMapping("index/catalog.json")
    public Map<String,List<Catelog2Vo>> getCatelogJson() {
        return categoryService.getCatelogJson();
    }
    @ResponseBody
    @GetMapping("evict")
    @CacheEvict(value = "category",key = "'getLevel1Categories'") //失效模式
    public String evict() {
        return "evict";
    }
}
