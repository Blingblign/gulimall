package com.zzclearning.gulimall.search.service;

import com.zzclearning.gulimall.search.vo.SearchParamsVo;
import com.zzclearning.gulimall.search.vo.SearchResult;

import java.io.IOException;
import java.util.List;

/**
 * @author bling
 * @create 2023-02-06 21:09
 */
public interface SearchService {
    SearchResult lookUpEs(SearchParamsVo searchParamsVo) throws IOException;

    List<String> getSuggestion(String keyWord);
}
