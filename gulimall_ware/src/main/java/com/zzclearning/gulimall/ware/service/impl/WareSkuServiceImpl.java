package com.zzclearning.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzclearning.common.constant.OrderStatusEnum;
import com.zzclearning.common.utils.R;
import com.zzclearning.gulimall.ware.constant.StockLockStatus;
import com.zzclearning.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.zzclearning.gulimall.ware.entity.WareOrderTaskEntity;
import com.zzclearning.gulimall.ware.exception.StockNotEnoughException;
import com.zzclearning.gulimall.ware.feign.OrderFeignService;
import com.zzclearning.gulimall.ware.feign.ProductFeignService;
import com.zzclearning.gulimall.ware.service.WareOrderTaskDetailService;
import com.zzclearning.gulimall.ware.service.WareOrderTaskService;
import com.zzclearning.to.SkuHaStockVo;
import com.zzclearning.to.mq.StockLockedTo;
import com.zzclearning.vo.SkuLockStockVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.Query;

import com.zzclearning.gulimall.ware.dao.WareSkuDao;
import com.zzclearning.gulimall.ware.entity.WareSkuEntity;
import com.zzclearning.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    WareSkuDao wareSkuDao;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    WareOrderTaskService wareOrderTaskService;
    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    OrderFeignService orderFeignService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String wareId = (String) params.get("wareId");
        String skuId = (String) params.get("skuId");
        LambdaQueryWrapper<WareSkuEntity> wrapper = Wrappers.lambdaQuery(WareSkuEntity.class);
        wrapper.eq(StringUtils.isNotBlank(wareId),WareSkuEntity::getWareId,wareId).eq(StringUtils.isNotBlank(skuId),WareSkuEntity::getSkuId,skuId);
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public Boolean updateWareSku(Long skuId, Integer skuNum, Long wareId) {
        List<WareSkuEntity> list = this.list(Wrappers.lambdaQuery(WareSkuEntity.class).eq(WareSkuEntity::getSkuId, skuId).eq(WareSkuEntity::getWareId, wareId));
        if (list.size() == 0) {
            //新建库存
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            //TODO 调用远程服务有异常无需回滚
            R info = productFeignService.info(skuId);
            if (info.getCode() == 0) {
                Map<String, Object> skuInfo = (Map<String, Object>)info.get("skuInfo");
                String skuName = (String)skuInfo.get("skuName");
                wareSkuEntity.setSkuName(skuName);
            }
            this.save(wareSkuEntity);
        } else {
            //更新库存
            wareSkuDao.updateSkuNum(skuId,skuNum,wareId);
        }
        return true;

    }

    @Override
    public List<SkuHaStockVo> getStock(List<Long> skuIds) {

       return wareSkuDao.getStock(skuIds);
    }

    @Transactional
    @Override
    public void orderLockStock(SkuLockStockVo skuLockStockVo) {
        //创建工作单
        WareOrderTaskEntity orderTask = new WareOrderTaskEntity();
        BeanUtils.copyProperties(skuLockStockVo,orderTask);
        wareOrderTaskService.save(orderTask);
        //查看是否有足够库存
        List<WareOrderTaskDetailEntity> orderTaskDetailEntities = new ArrayList<>();
        List<SkuLockStockVo.SkuOrderLockItem> stockLocks = skuLockStockVo.getItems();
        stockLocks.forEach(stockLockVo -> {
            //查看sku在哪些仓库库存足够
            boolean isLocked = false;
            List<Long> wareIds = wareSkuDao.skuWareHasStock(stockLockVo.getSkuId(), stockLockVo.getSkuNum());
            if (wareIds != null && wareIds.size() > 0) {
                for (Long wareId : wareIds) {
                    //锁定库存
                    int count = wareSkuDao.updateStockLock(stockLockVo.getSkuId(), wareId, stockLockVo.getSkuNum());
                    if (count == 1) {

                        //添加工作单详情
                        WareOrderTaskDetailEntity orderTaskDetail = new WareOrderTaskDetailEntity();
                        BeanUtils.copyProperties(stockLockVo,orderTaskDetail);
                        orderTaskDetail.setTaskId(orderTask.getId());
                        orderTaskDetail.setWareId(wareId);
                        orderTaskDetail.setLockStatus(StockLockStatus.STOCK_LOCKED.getCode());
                        wareOrderTaskDetailService.save(orderTaskDetail);
                        //向RabbitMQ发送库存锁定消息给延时队列
                        StockLockedTo stockLockedTo = new StockLockedTo();
                        stockLockedTo.setId(orderTask.getId());
                        stockLockedTo.setDetailId(orderTaskDetail.getId());
                        rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",stockLockedTo);
                        isLocked = true;//改变状态
                        break;
                    }
                }
                if (!isLocked) {
                    throw new StockNotEnoughException("商品库存不足，锁定库存失败");
                }
            } else {
                throw new StockNotEnoughException("商品库存不足，锁定库存失败");
            }
        });
    }

    @Override
    public void handleOrderLock(String orderSn) {
        //根据订单号查询工作单信息
        WareOrderTaskEntity orderTask = wareOrderTaskService.getOne(new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", orderSn));
        if (orderTask != null) {
            //工作单不为空，查询工作单详情
            List<WareOrderTaskDetailEntity> details = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", orderTask.getId()));
            if (details != null && details.size() > 0) {

                //只有已锁定状态才解锁库存并修改工作单详情状态
                details.stream().filter(detail-> detail.getLockStatus() == StockLockStatus.STOCK_LOCKED.getCode()).forEach(this::unlockStockAndChangeDetailItemStatus);
            }
        }
    }

    @Override
    public void handleStockLock(StockLockedTo stockLockedTo) {
        //查询工作单id是否存在
        WareOrderTaskEntity orderTask = wareOrderTaskService.getById(stockLockedTo.getId());
        if (orderTask != null) {
            //查询订单支付状态
            R r = orderFeignService.getOrderInfo(orderTask.getOrderSn());
            OrderVo orderVo = r.getData(new TypeReference<OrderVo>() {
            });
            //订单不存在或订单新建或订单已取消，解锁库存
            if (orderVo == null || Objects.equals(orderVo.getStatus(), OrderStatusEnum.ORDER_NEW.getStatus()) || Objects.equals(orderVo.getStatus(), OrderStatusEnum.ORDER_CLOSED.getStatus())) {
                //查询工作单详情
                WareOrderTaskDetailEntity detail = wareOrderTaskDetailService.getById(stockLockedTo.getDetailId());
                //已锁定状态才解锁
                if (detail.getLockStatus() == StockLockStatus.STOCK_LOCKED.getCode()) {
                    //解锁库存,并修改工作单详情状态为已解锁
                    unlockStockAndChangeDetailItemStatus(detail);
                }


            }
        }
    }

    @Override
    public void handleOrderFinish(String orderSn) {
        WareOrderTaskEntity orderTask = wareOrderTaskService.getOne(new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", orderSn));
        if (orderTask != null) {
            //工作单不为空，查询工作单详情
            List<WareOrderTaskDetailEntity> details = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", orderTask.getId()));
            if (details != null && details.size() > 0) {

                //只有已锁定状态才扣减库存并修改工作单详情状态 TODO 如果有已解锁的库存抛异常另作处理
                details.stream().filter(detail-> detail.getLockStatus() == StockLockStatus.STOCK_LOCKED.getCode()).forEach(this::reduceSkuStock);
            }
        }
    }

    /**
     * 扣减库存，修改工作单状态
     * @param detail
     */
    private void reduceSkuStock(WareOrderTaskDetailEntity detail) {
        wareSkuDao.reduceSkuStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum());
        log.info("扣减商品库存，工作单详情为{}",detail);
        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
        wareOrderTaskDetailEntity.setId(detail.getId());
        wareOrderTaskDetailEntity.setLockStatus(StockLockStatus.STOCK_DEDUCED.getCode());
        wareOrderTaskDetailService.updateById(wareOrderTaskDetailEntity);
        System.out.println("修改订单项,id:"+detail.getId() + "的状态");
    }

    private void unlockStockAndChangeDetailItemStatus(WareOrderTaskDetailEntity detail) {
        wareSkuDao.releaseStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum());
        System.out.println("释放库存，skuId:" + detail.getSkuId() + "，数量：" + detail.getSkuNum());
        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
        wareOrderTaskDetailEntity.setId(detail.getId());
        wareOrderTaskDetailEntity.setLockStatus(StockLockStatus.STOCK_RELEASED.getCode());
        wareOrderTaskDetailService.updateById(wareOrderTaskDetailEntity);
        System.out.println("修改订单项,id:"+detail.getId() + "的状态");

    }


    @Data
    static class OrderVo {
        private Long id;
        private Integer status;
    }


}