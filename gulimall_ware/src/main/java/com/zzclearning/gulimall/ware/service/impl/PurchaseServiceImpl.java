package com.zzclearning.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzclearning.gulimall.ware.constant.WareConstant;
import com.zzclearning.gulimall.ware.entity.PurchaseDetailEntity;
import com.zzclearning.gulimall.ware.entity.WareSkuEntity;
import com.zzclearning.gulimall.ware.service.PurchaseDetailService;
import com.zzclearning.gulimall.ware.service.WareSkuService;
import com.zzclearning.gulimall.ware.vo.DonePurchaseVo;
import com.zzclearning.gulimall.ware.vo.PurchaseItem;
import com.zzclearning.gulimall.ware.vo.MergeDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.Query;

import com.zzclearning.gulimall.ware.dao.PurchaseDao;
import com.zzclearning.gulimall.ware.entity.PurchaseEntity;
import com.zzclearning.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {
    @Autowired
    PurchaseDetailService purchaseDetailService;
    @Autowired
    WareSkuService wareSkuService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseEntity> wrapper = Wrappers.lambdaQuery(PurchaseEntity.class);
        String status = (String) params.get("status");
        String key = (String)params.get("key");
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(PurchaseEntity::getStatus,status);
        }
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(w->{
                w.like(PurchaseEntity::getId,key).or().like(PurchaseEntity::getWareId,key).or().like(PurchaseEntity::getAssigneeName,key);
            });
        }
        IPage<PurchaseEntity> page = this.page(new Query<PurchaseEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public PageUtils getUnreceivedPurchase() {
        LambdaQueryWrapper<PurchaseEntity> wrapper = Wrappers.lambdaQuery(PurchaseEntity.class)
                .eq(PurchaseEntity::getStatus, 0).or().eq(PurchaseEntity::getStatus, 1);
        IPage<PurchaseEntity> page = this.page(new Query<PurchaseEntity>().getPage(new HashMap<>()), wrapper);
        return new PageUtils(page);
    }

    @Override
    public Boolean mergePurchaseDetails(MergeDetailVo mergeDetailVo) {
        Long purchaseId = mergeDetailVo.getPurchaseId();
        //没有采购单id，新建采购单
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(0);
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        Long finalPurchaseId = purchaseId;
        PurchaseEntity purchaseEntity = this.getById(finalPurchaseId);
        if (!Objects.equals(purchaseEntity.getStatus(), WareConstant.PurchaseType.CREATED.getCode()) && !Objects.equals(purchaseEntity.getStatus(), WareConstant.PurchaseType.ASSIGNED.getCode())) {
            log.info("采购单id：{}，状态：{}",finalPurchaseId,purchaseEntity.getStatus());
            throw new RuntimeException("合并采购需求失败，该采购单不处于新建或已分配状态");
        }
        List<PurchaseDetailEntity> purchaseDetailEntities = mergeDetailVo.getItems().stream().map(item -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setId(item);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailType.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(purchaseDetailEntities);
        return true;
    }

    @Override
    public Boolean receivePurchases(List<Long> ids) {
        // TODO 查询采购单是否被更改
        //将采购需求状态改为正在采购
        purchaseDetailService.update(Wrappers.lambdaUpdate(PurchaseDetailEntity.class)
                                             .in(PurchaseDetailEntity::getPurchaseId,ids).set(PurchaseDetailEntity::getStatus,WareConstant.PurchaseDetailType.RECEIVED.getCode()));
        //采购单状态改为已领取
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(id);
            purchaseEntity.setStatus(WareConstant.PurchaseType.RECEIVED.getCode());
            return purchaseEntity;
        }).collect(Collectors.toList());
        this.updateBatchById(collect);
        return true;
    }
    @Transactional
    @Override
    public Boolean purchaseDone(DonePurchaseVo doneVo) {
        // 查看是否有采购失败项
        Long purchaseId = doneVo.getId();
        boolean flag = true;
        //更改采购需求状态
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItem purchaseItem : doneVo.getItems()) {
            PurchaseDetailEntity purchaseDetailEntity = purchaseDetailService.getById(purchaseItem.getItemId());
            if (Objects.equals(purchaseItem.getStatus(), WareConstant.PurchaseDetailType.ERRORED.getCode())) {
                log.info("采购失败，采购需求id为：{}，失败原因为：{}", purchaseItem.getItemId(),purchaseItem.getReason());
                flag = false;
            }else {
                //根据skuId和仓库id更新库存
                Long skuId = purchaseDetailEntity.getSkuId();
                Integer skuNum = purchaseDetailEntity.getSkuNum();
                Long wareId = purchaseDetailEntity.getWareId();
                Boolean result = wareSkuService.updateWareSku(skuId,skuNum,wareId);

            }
            purchaseDetailEntity.setStatus(purchaseItem.getStatus());
            updates.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(updates);
        //更改采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setStatus(flag?WareConstant.PurchaseType.FINISHED.getCode() : WareConstant.PurchaseType.ERRORED.getCode());
        this.updateById(purchaseEntity);
        return true;
    }

}