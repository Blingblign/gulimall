package com.zzclearning.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.gulimall.ware.entity.PurchaseEntity;
import com.zzclearning.gulimall.ware.vo.DonePurchaseVo;
import com.zzclearning.gulimall.ware.vo.MergeDetailVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 22:03:50
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils getUnreceivedPurchase();

    Boolean mergePurchaseDetails(MergeDetailVo mergeDetailVo);

    Boolean receivePurchases(List<Long> ids);

    Boolean purchaseDone(DonePurchaseVo doneVo);
}

