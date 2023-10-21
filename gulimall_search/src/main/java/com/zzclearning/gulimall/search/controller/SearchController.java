package com.zzclearning.gulimall.search.controller;

import com.zzclearning.common.utils.R;
import com.zzclearning.gulimall.search.service.SearchService;
import com.zzclearning.gulimall.search.vo.SearchParamsVo;
import com.zzclearning.gulimall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

/**
 * 构造检索条件：
 *      keyword=小米&sort=saleCount_desc/asc&hasStock=0/1&skuPrice=400_1900&brandId=1&catalog3Id=1&attrs=1_3G:4G:5G&attrs=2_骁龙845&attrs=4_高清屏
 * @author bling
 * @create 2023-02-03 21:12
 */
@Slf4j
@Controller
public class SearchController {
    @Autowired
    SearchService searchService;

    @GetMapping({"/","/list.html"})
    public String toSearchPage(SearchParamsVo searchParamsVo, Model model) {
        SearchResult searchResult = null;
        try {
            searchResult = searchService.lookUpEs(searchParamsVo);
        } catch (IOException e) {
            log.error("检索es中数据失败...",e);
        }
        model.addAttribute("result",searchResult);
        return "list";
    }

    /**
     * 自动补全功能
     * @param keyWord 关键字
     *
     * @return
     */
    @ResponseBody
    @GetMapping({"/autocomplete"})
    public R searchComplete(String keyWord) {
        if (StringUtils.isEmpty(keyWord)) {
            return R.error("搜索字段不能为空");
        }
        List<String> list = searchService.getSuggestion(keyWord);
        return R.ok().put("data",list);
    }
}
