package com.zzclearning.gulimall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzclearning.common.utils.R;
import com.zzclearning.gulimall.order.config.AlipayTemplate;
import com.zzclearning.gulimall.order.config.MyRabbitMQConfig;
import com.zzclearning.gulimall.order.constant.OrderConstant;
import com.zzclearning.common.constant.OrderStatusEnum;
import com.zzclearning.gulimall.order.entity.OrderItemEntity;
import com.zzclearning.gulimall.order.entity.PaymentInfoEntity;
import com.zzclearning.gulimall.order.feign.CartFeignService;
import com.zzclearning.gulimall.order.feign.MemberFeignService;
import com.zzclearning.gulimall.order.feign.ProductFeignService;
import com.zzclearning.gulimall.order.feign.WareFeignService;
import com.zzclearning.gulimall.order.interceptor.OrderInterceptor;
import com.zzclearning.gulimall.order.service.OrderItemService;
import com.zzclearning.gulimall.order.service.PaymentInfoService;
import com.zzclearning.gulimall.order.to.OrderCreateTo;
import com.zzclearning.gulimall.order.vo.*;
import com.zzclearning.to.MemberEntityVo;
import com.zzclearning.to.MemberReceiveAddressTo;
import com.zzclearning.to.SkuHaStockVo;
import com.zzclearning.vo.AddressFare;
import com.zzclearning.vo.SkuLockStockVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzclearning.common.utils.PageUtils;
import com.zzclearning.common.utils.Query;

import com.zzclearning.gulimall.order.dao.OrderDao;
import com.zzclearning.gulimall.order.entity.OrderEntity;
import com.zzclearning.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ThreadPoolExecutor  executor;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    AlipayTemplate alipayTemplate;
    @Autowired
    PaymentInfoService paymentInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public ConfirmOrderVo orderConfirm() throws ExecutionException, InterruptedException {
        ConfirmOrderVo confirmOrderVo = new ConfirmOrderVo();
        MemberEntityVo user = OrderInterceptor.userInfo.get();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //根据用户id查询用户收货地址
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            R res = memberFeignService.getUserAddresses(user.getId());
            List<MemberReceiveAddressTo> addresses = res.getData(new TypeReference<List<MemberReceiveAddressTo>>() {
            });
            confirmOrderVo.setMemberAddressVos(addresses);
        }, executor);
        //查询用户选中的购物车列表
        CompletableFuture<Void> itemsFuture = CompletableFuture.runAsync(() -> {
            // feign远程调用/异步调用丢失请求头问题 RequestInterceptor
            RequestContextHolder.setRequestAttributes(requestAttributes);
            R r = cartFeignService.getCheckedCartItemList();
            List<OrderItem> orderItems = r.getData(new TypeReference<List<OrderItem>>() {
            });
            confirmOrderVo.setItems(orderItems);
        }, executor);
        CompletableFuture<Void> sumFuture = itemsFuture.thenRunAsync(() -> {
            //商品总数量,总商品金额
            Integer count = 0;
            List<OrderItem> orderItems = confirmOrderVo.getItems();
            BigDecimal price = new BigDecimal(0);
            for (OrderItem orderItem : orderItems) {
                count += orderItem.getCount();
                price = price.add(orderItem.getPrice().multiply(new BigDecimal(orderItem.getCount())));
            }
            confirmOrderVo.setCount(count);
            confirmOrderVo.setTotal(price);
        }, executor);
        //查询商品是否有货
        CompletableFuture<Void> stockFuture = itemsFuture.thenRunAsync(() -> {
            R res = wareFeignService.skusHasStock(confirmOrderVo.getItems().stream().map(OrderItem::getSkuId).collect(Collectors.toList()));
            List<SkuHaStockVo> haStockVos = res.getData(new TypeReference<List<SkuHaStockVo>>() {
            });
            Map<Long, Boolean> hasStockMap = haStockVos.stream().collect(Collectors.toMap(SkuHaStockVo::getSkuId, vo -> vo.getStock() > 0));
            confirmOrderVo.setStocks(hasStockMap);
        }, executor);

        //防刷令牌，保证幂等性
        CompletableFuture<Void> tokenFuture = CompletableFuture.runAsync(() -> {
            String uniqueToken = UUID.randomUUID().toString().replace("-", "");
            redisTemplate.opsForValue().set(OrderConstant.ORDER_TOKEN_PREFIX + user.getId(),uniqueToken);
            confirmOrderVo.setUniqueToken(uniqueToken);
        },executor);

        //等待异步任务全部完成
        CompletableFuture.allOf(addressFuture, itemsFuture, sumFuture, stockFuture, tokenFuture).get();
        return confirmOrderVo;
    }
    @Transactional//开启本地事务
    @Override
    public SubmitResponseVo submitOrder(SubmitOrderVo submitOrderVo) {
        SubmitResponseVo response = new SubmitResponseVo();
        MemberEntityVo userInfo = OrderInterceptor.userInfo.get();
        //原子性验令牌
        String orderToken = submitOrderVo.getUniqueToken();
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        //返回值0--失败，1--成功
        Long res = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Collections.singletonList(OrderConstant.ORDER_TOKEN_PREFIX + userInfo.getId()), orderToken);
        if (res == 0) {
            //令牌失效，
            response.setCode(1);
            return response;
        }
        //创建订单，订单项
        OrderCreateTo orderCreateTo = createOrder(submitOrderVo);
        //验价
        boolean b = comparePrice(submitOrderVo.getPayPrice(), orderCreateTo.getPayPrice());
        if (!b) {
            //超出误差范围
            response.setCode(2);
            return response;
        }
        //保存订单,订单项到数据库
        saveOrder(orderCreateTo);
        //向RabbitMQ发送订单创建消息
        rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",orderCreateTo.getOrder().getOrderSn());
        //锁库存
        OrderEntity order = orderCreateTo.getOrder();
        SkuLockStockVo skuLockStockVo = new SkuLockStockVo();
        skuLockStockVo.setOrderSn(order.getOrderSn());
        String join = String.join(" ", order.getReceiverProvince(), order.getReceiverCity(), order.getReceiverRegion(), order.getReceiverDetailAddress());
        skuLockStockVo.setDeliveryAddress(join);
        skuLockStockVo.setConsignee(order.getReceiverName());
        skuLockStockVo.setConsigneeTel(order.getReceiverPhone());
        List<SkuLockStockVo.SkuOrderLockItem> lockItems = orderCreateTo.getOrderItems().stream().map((item) -> {
            SkuLockStockVo.SkuOrderLockItem skuOrderLockItem = new SkuLockStockVo.SkuOrderLockItem();
            skuOrderLockItem.setSkuId(item.getSkuId());
            skuOrderLockItem.setSkuName(item.getSkuName());
            skuOrderLockItem.setSkuNum(item.getSkuQuantity());
            return skuOrderLockItem;
        }).collect(Collectors.toList());
        skuLockStockVo.setItems(lockItems);
        R r = wareFeignService.lockStock(skuLockStockVo);
        if (r.getCode() != 0) {
            response.setCode(3);
            return response;
        }
        response.setOrder(order);
        return response;
        //后续优惠扣减（不做）
    }

    @Override
    public void closeOrder(String orderSn) {
        //查询订单状态
        OrderEntity order = this.getOne(new LambdaQueryWrapper<OrderEntity>().eq(OrderEntity::getOrderSn, orderSn));
        if (order != null && Objects.equals(order.getStatus(), OrderStatusEnum.ORDER_NEW.getStatus())) {
            //定时关单，手动解锁库存 TODO 乐观锁
            OrderEntity orderEntity = new OrderEntity();
            orderEntity.setId(order.getId());
            orderEntity.setStatus(OrderStatusEnum.ORDER_CLOSED.getStatus());
            //发送消息给mq，解锁订单锁定的库存
            rabbitTemplate.convertAndSend("order-event-exchange","order.release.other.#",orderSn);
            System.out.println("定时关单---发送消息给mq，解锁订单锁定的库存");
            this.updateById(orderEntity);
            System.out.println("定时关单---更改订单状态");
        }
    }

    @Override
    public PayVo getPayVo(String orderSn) {
        OrderEntity order = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        if (order != null && Objects.equals(order.getStatus(), OrderStatusEnum.ORDER_NEW.getStatus())) {

            PayVo payVo = new PayVo();
            payVo.setOrderSn(orderSn);
            long createTime = order.getCreateTime().getTime();
            long timeExpress = createTime + MyRabbitMQConfig.ORDER_DELAY_TIME_EXPIRE;//设置绝对超时时间
            Date date = new Date(timeExpress);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String format = simpleDateFormat.format(date);
            payVo.setTimeExpire(format);
            BigDecimal totalAmount = order.getPayAmount().setScale(2, RoundingMode.UP);
            payVo.setTotalAmount(totalAmount.toString());
            //查询第一个订单项skuName
            Optional<OrderItemEntity> orderItem = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn)).stream().findFirst();
            orderItem.ifPresent(orderItemEntity -> payVo.setSubject(orderItemEntity.getSkuName()));
            return payVo;
        }
        return null;
    }

    @Transactional
    @Override
    public String handleAlipayNotify(AliPayAsyncVO aliPayAsyncVO, HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException {
        boolean verified = alipayTemplate.verifySign(request);
        if (!verified) return "fail";
        log.info("验签通过...");
        //验签成功,验证交易号，交易金额，appId
        //1、需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
        String out_trade_no = aliPayAsyncVO.getOut_trade_no();
        OrderEntity order = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", out_trade_no));
        if (order == null ) return "fail";
        //2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
        //3、校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
        //4、验证app_id是否为该商户本身。
        String payAmount = order.getPayAmount().setScale(2, RoundingMode.UP).toString();
        if (!aliPayAsyncVO.getTotal_amount().equals(payAmount) || !aliPayAsyncVO.getApp_id().equals(AlipayTemplate.app_id)) {
            return "fail";
        }
        log.info("验证交易信息通过...");
        //保存交易流水
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setOrderSn(out_trade_no);
        paymentInfoEntity.setAlipayTradeNo(aliPayAsyncVO.getTrade_no());
        paymentInfoEntity.setPaymentStatus(aliPayAsyncVO.getTrade_status());
        paymentInfoEntity.setSubject(aliPayAsyncVO.getSubject());
        paymentInfoEntity.setTotalAmount(new BigDecimal(aliPayAsyncVO.getTotal_amount()));
        paymentInfoEntity.setCreateTime(new Date());
        paymentInfoEntity.setCallbackTime(aliPayAsyncVO.getNotify_time());
        paymentInfoService.save(paymentInfoEntity);
        if (aliPayAsyncVO.getTrade_status().equals("TRADE_FINISHED") || aliPayAsyncVO.getTrade_status().equals("TRADE_SUCCESS")) {
            //修改订单状态为已付款
            OrderEntity orderEntity = new OrderEntity();
            orderEntity.setId(order.getId());
            orderEntity.setStatus(OrderStatusEnum.ORDER_READY.getStatus());
            this.updateById(orderEntity);
            //向RabbitMQ发送库存扣减消息
            rabbitTemplate.convertAndSend("order-even-exchange","order.finish",out_trade_no);
            log.info("向RabbitMQ发送库存扣减消息...");
            return "success";
        }
       return "fail";
    }

    private OrderCreateTo createOrder(SubmitOrderVo submitOrderVo) {
        String orderSn = UUID.randomUUID().toString().replace("-", "");
        //构造订单项数据--远程获取已选中购物车项
        R r = cartFeignService.getCheckedCartItemList();
        List<OrderItem> orderItems = r.getData(new TypeReference<List<OrderItem>>() {
        });
        Map<Long, OrderItem> orderItemMap = orderItems.stream().collect(Collectors.toMap(OrderItem::getSkuId, orderItem -> orderItem));
        //远程查询sku对应的spu、catalog信息
        List<Long> skuIds = orderItems.stream().map(OrderItem::getSkuId).collect(Collectors.toList());
        R infos = productFeignService.getOrderItemInfoBySkuIds(skuIds);
        List<OrderItemEntity> orderItemEntities = infos.getData(new TypeReference<List<OrderItemEntity>>() {
        });
        //构造订单项信息
        orderItemEntities.forEach(orderItemEntity -> {
            OrderItem item = orderItemMap.get(orderItemEntity.getSkuId());
            orderItemEntity.setOrderSn(orderSn);
            orderItemEntity.setSkuAttrsVals(JSON.toJSONString(item.getSkuAttrValues()));
            orderItemEntity.setSkuQuantity(item.getCount());
            orderItemEntity.setSkuPic(item.getImage());
            orderItemEntity.setSkuPrice(item.getPrice());
            //TODO 优惠扣减
            orderItemEntity.setPromotionAmount(new BigDecimal(0));
            orderItemEntity.setIntegrationAmount(new BigDecimal(0));
            orderItemEntity.setCouponAmount(new BigDecimal(0));
            orderItemEntity.setRealAmount(orderItemEntity.getPromotionAmount().add(orderItemEntity.getIntegrationAmount().add(orderItemEntity.getCouponAmount())));
            orderItemEntity.setGiftIntegration(item.getPrice().intValue());
            orderItemEntity.setGiftGrowth(item.getPrice().intValue()/10);
        });

        //构造订单数据
        MemberEntityVo userInfo = OrderInterceptor.userInfo.get();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setMemberId(userInfo.getId());
        orderEntity.setMemberUsername(userInfo.getUsername());
        orderEntity.setOrderSn(orderSn);
        orderEntity.setCreateTime(new Date());
        BigDecimal totalAmount = new BigDecimal(0);
        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            totalAmount = totalAmount.add(orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity())).subtract(orderItemEntity.getRealAmount()));
        }
        orderEntity.setTotalAmount(totalAmount);
        //远程查询收货地址和邮费
        R fareResult = wareFeignService.calculateFare(submitOrderVo.getAddrId());
        AddressFare addressFare = fareResult.getData(new TypeReference<AddressFare>() {
        });
        orderEntity.setFreightAmount(addressFare.getFare());
        orderEntity.setPayAmount(totalAmount.add(orderEntity.getFreightAmount()));
        //收货地址
        MemberReceiveAddressTo address = addressFare.getAddress();
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverRegion(address.getRegion());
        //to
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(orderItemEntities);
        orderCreateTo.setPayPrice(orderEntity.getPayAmount());
        return orderCreateTo;
    }

    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        List<OrderItemEntity> orderItemEntities = order.getOrderItems();
        //总优惠信息，积分，成长值
        BigDecimal promotionAmount = new BigDecimal(0);
        BigDecimal integrationAmount = new BigDecimal(0);
        BigDecimal couponAmount = new BigDecimal(0);
        BigDecimal realAmount = new BigDecimal(0);
        Integer integration = 0;
        Integer growth = 0;
        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            promotionAmount = promotionAmount.add(orderItemEntity.getPromotionAmount());
            integrationAmount = integrationAmount.add(orderItemEntity.getIntegrationAmount());
            couponAmount = couponAmount.add(orderItemEntity.getCouponAmount());
            realAmount = realAmount.add(orderItemEntity.getRealAmount());
            integration += orderItemEntity.getGiftIntegration();
            growth += orderItemEntity.getGiftGrowth();
        }
        orderEntity.setPromotionAmount(promotionAmount);
        orderEntity.setIntegrationAmount(integrationAmount);
        orderEntity.setCouponAmount(couponAmount);
        orderEntity.setDiscountAmount(realAmount);
        orderEntity.setIntegration(integration);
        orderEntity.setGrowth(growth);

        //订单状态
        orderEntity.setStatus(OrderStatusEnum.ORDER_NEW.getStatus());

        //删除状态
        orderEntity.setDeleteStatus(0);
        //保存订单项、订单信息
        orderItemService.saveBatch(orderItemEntities);
        this.save(orderEntity);
    }

    /**
     *误差应小于等于0.01
     * @param pagePayPrice 页面展示支付价格
     * @param payAmount 后台计算后应支付价格
     * @return
     */
    private boolean comparePrice(BigDecimal pagePayPrice,BigDecimal payAmount) {
        BigDecimal subtract = pagePayPrice.subtract(payAmount);
        if (subtract.compareTo(new BigDecimal(0)) > 0) {
            return subtract.compareTo(new BigDecimal("0.01")) < 1;
        }
        if (subtract.compareTo(new BigDecimal(0)) < 0) {
            return subtract.compareTo(new BigDecimal("-0.01")) > -1;
        }
        return true;

    }

}