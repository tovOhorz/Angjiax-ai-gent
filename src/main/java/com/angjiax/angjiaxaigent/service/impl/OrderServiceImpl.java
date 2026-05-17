package com.angjiax.angjiaxaigent.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.angjiax.angjiaxaigent.entity.*;
import com.angjiax.angjiaxaigent.entity.dto.order.OrderAddRequest;
import com.angjiax.angjiaxaigent.entity.dto.order.OrderQueryRequest;
import com.angjiax.angjiaxaigent.entity.vo.cart.CartVO;
import com.angjiax.angjiaxaigent.entity.vo.order.OrderVO;
import com.angjiax.angjiaxaigent.exception.BusinessException;
import com.angjiax.angjiaxaigent.exception.ErrorCode;
import com.angjiax.angjiaxaigent.mapper.OrderItemMapper;
import com.angjiax.angjiaxaigent.mapper.OrderMapper;
import com.angjiax.angjiaxaigent.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author t'h
 * @description 针对表【order(订单)】的数据库操作Service实现
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
        implements OrderService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserCouponService userCouponService;

    @Autowired
    private CartService cartService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId, OrderAddRequest orderAddRequest) {
        if (userId == null || orderAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long productId = orderAddRequest.getProductId();
        Integer quantity = orderAddRequest.getQuantity();
        Long couponId = orderAddRequest.getCouponId();
        String address = orderAddRequest.getAddress();

        // 1. 获取商品信息
        Product product = productService.getById(productId);
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品不存在或已下架");
        }

        // 2. 检查库存
        if (product.getStock() < quantity) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "库存不足");
        }

        // 3. 计算金额
        BigDecimal totalAmount = product.getCurrentPrice().multiply(new BigDecimal(quantity));
        BigDecimal discountAmount = BigDecimal.ZERO;

        // 4. 处理优惠券
        if (couponId != null) {
            UserCoupon userCoupon = userCouponService.getValidUserCoupon(userId, couponId);
            if (userCoupon == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券不存在或已使用");
            }
            Coupon coupon = couponService.validateCoupon(couponId, totalAmount);
            discountAmount = couponService.calculateDiscount(coupon, totalAmount);
        }

        BigDecimal actualPaid = totalAmount.subtract(discountAmount);
        if (actualPaid.compareTo(BigDecimal.ZERO) < 0) {
            actualPaid = BigDecimal.ZERO;
        }

        // 5. 扣减库存
        product.setStock(product.getStock() - quantity);
        productService.updateById(product);

        // 6. 生成订单
        String orderIdStr = generateOrderId();
        Order order = new Order();
        order.setOrderId(orderIdStr);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setActualPaid(actualPaid);
        order.setCouponId(couponId);
        order.setOrderStatus(1);
        order.setAddress(address);

        boolean saved = this.save(order);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建订单失败");
        }

        // 7. 创建订单明细
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderIdStr);
        orderItem.setProductId(productId);
        orderItem.setProductName(product.getProductName());
        orderItem.setProductImage(product.getImageUrl());
        orderItem.setQuantity(quantity);
        orderItem.setUnitPrice(product.getCurrentPrice());
        orderItem.setTotalPrice(totalAmount);
        orderItemMapper.insert(orderItem);

        // 8. 标记优惠券已使用
        if (couponId != null) {
            userCouponService.markUsed(userId, couponId, orderIdStr);
        }

        // 9. 清空购物车中该商品
        cartService.removeFromCart(userId, productId);

        log.info("单商品订单创建成功，订单号：{}，用户ID：{}，实付金额：{}", orderIdStr, userId, actualPaid);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrderFromCart(Long userId, List<Long> cartItemIds, Long couponId, String address) {
        if (userId == null || CollUtil.isEmpty(cartItemIds)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择要购买的商品");
        }

        // 1. 获取购物车商品
        List<CartVO> cartItems = cartService.getCartItemsByIds(userId, cartItemIds);
        if (CollUtil.isEmpty(cartItems)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "购物车商品不存在");
        }

        // 2. 计算总金额、校验库存、构建订单项
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartVO item : cartItems) {
            Product product = productService.getById(item.getProductId());
            if (product == null || product.getStatus() != 1) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品已下架：" + item.getProductName());
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品库存不足：" + item.getProductName());
            }

            BigDecimal itemTotal = product.getCurrentPrice().multiply(new BigDecimal(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            // 构建订单明细
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getProductName());
            orderItem.setProductImage(product.getImageUrl());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(product.getCurrentPrice());
            orderItem.setTotalPrice(itemTotal);
            orderItems.add(orderItem);

            // 扣减库存
            product.setStock(product.getStock() - item.getQuantity());
            productService.updateById(product);
        }

        // 3. 处理优惠券
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (couponId != null) {
            UserCoupon userCoupon = userCouponService.getValidUserCoupon(userId, couponId);
            if (userCoupon == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券不存在或已使用");
            }
            Coupon coupon = couponService.validateCoupon(couponId, totalAmount);
            discountAmount = couponService.calculateDiscount(coupon, totalAmount);
        }

        BigDecimal actualPaid = totalAmount.subtract(discountAmount);
        if (actualPaid.compareTo(BigDecimal.ZERO) < 0) {
            actualPaid = BigDecimal.ZERO;
        }

        // 4. 创建订单
        String orderIdStr = generateOrderId();
        Order order = new Order();
        order.setOrderId(orderIdStr);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setActualPaid(actualPaid);
        order.setCouponId(couponId);
        order.setOrderStatus(1);
        order.setAddress(address);

        boolean saved = this.save(order);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建订单失败");
        }

        // 5. 保存订单明细
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(orderIdStr);
            orderItemMapper.insert(orderItem);
        }
        order.setOrderItems(orderItems);

        // 6. 标记优惠券已使用
        if (couponId != null) {
            userCouponService.markUsed(userId, couponId, orderIdStr);
        }

        // 7. 清空购物车
        for (Long cartId : cartItemIds) {
            cartService.removeFromCart(userId, cartId);
        }

        log.info("多商品订单创建成功，订单号：{}，用户ID：{}，商品数：{}，实付金额：{}",
                orderIdStr, userId, orderItems.size(), actualPaid);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean payOrder(Long userId, String orderId) {
        Order order = getOrderByOrderId(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        if (order.getOrderStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单状态不正确，无法支付");
        }

        order.setOrderStatus(2);
        order.setPaidTime(new Date());

        boolean updated = this.updateById(order);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "支付失败");
        }

        log.info("订单支付成功，订单号：{}", orderId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelOrder(Long userId, String orderId) {
        Order order = getOrderByOrderId(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        if (order.getOrderStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单状态不正确，无法取消");
        }

        // 恢复库存
        QueryWrapper<OrderItem> wrapper = new QueryWrapper<>();
        wrapper.eq("orderId", orderId);
        List<OrderItem> orderItems = orderItemMapper.selectList(wrapper);
        for (OrderItem item : orderItems) {
            Product product = productService.getById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productService.updateById(product);
            }
        }

        // 恢复优惠券
        if (order.getCouponId() != null) {
            userCouponService.restoreCoupon(userId, order.getCouponId(), orderId);
        }

        order.setOrderStatus(5);
        boolean updated = this.updateById(order);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "取消订单失败");
        }

        log.info("订单已取消，订单号：{}", orderId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean shipOrder(String orderId) {
        Order order = getOrderByOrderId(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        if (order.getOrderStatus() != 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单状态不正确，无法发货");
        }

        order.setOrderStatus(3);
        boolean updated = this.updateById(order);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发货失败");
        }

        log.info("订单已发货，订单号：{}", orderId);
        return true;
    }

    @Override
    public Boolean confirmOrder(Long userId, String orderId) {
        Order order = getOrderByOrderId(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        if (order.getOrderStatus() != 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单状态不正确，无法确认收货");
        }

        order.setOrderStatus(4);
        boolean updated = this.updateById(order);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "确认收货失败");
        }

        log.info("订单已确认收货，订单号：{}", orderId);
        return true;
    }

    @Override
    public OrderVO getOrderVO(Order order) {
        if (order == null) {
            return null;
        }

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);

        // 查询订单明细
        QueryWrapper<OrderItem> wrapper = new QueryWrapper<>();
        wrapper.eq("orderId", order.getOrderId());
        List<OrderItem> orderItems = orderItemMapper.selectList(wrapper);
        orderVO.setOrderItems(orderItems);

        // 设置订单状态文本
        String statusText;
        switch (order.getOrderStatus()) {
            case 1:
                statusText = "待付款";
                break;
            case 2:
                statusText = "待发货";
                break;
            case 3:
                statusText = "待收货";
                break;
            case 4:
                statusText = "已完成";
                break;
            case 5:
                statusText = "已取消";
                break;
            default:
                statusText = "未知";
        }
        orderVO.setOrderStatusText(statusText);

        return orderVO;
    }

    @Override
    public Page<OrderVO> getOrderVOPage(Long userId, OrderQueryRequest orderQueryRequest) {
        if (orderQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long current = orderQueryRequest.getCurrent();
        long pageSize = orderQueryRequest.getPageSize();

        QueryWrapper<Order> queryWrapper = getQueryWrapper(orderQueryRequest);
        queryWrapper.eq("userId", userId);
        queryWrapper.orderByDesc("createTime");

        Page<Order> orderPage = this.page(new Page<>(current, pageSize), queryWrapper);
        Page<OrderVO> orderVOPage = new Page<>(current, pageSize, orderPage.getTotal());

        List<OrderVO> orderVOList = orderPage.getRecords().stream()
                .map(this::getOrderVO)
                .collect(Collectors.toList());
        orderVOPage.setRecords(orderVOList);

        return orderVOPage;
    }

    @Override
    public QueryWrapper<Order> getQueryWrapper(OrderQueryRequest orderQueryRequest) {
        if (orderQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = orderQueryRequest.getId();
        String orderId = orderQueryRequest.getOrderId();
        Long userId = orderQueryRequest.getUserId();
        Integer orderStatus = orderQueryRequest.getOrderStatus();
        BigDecimal minTotalAmount = orderQueryRequest.getMinTotalAmount();
        BigDecimal maxTotalAmount = orderQueryRequest.getMaxTotalAmount();
        BigDecimal minActualPaid = orderQueryRequest.getMinActualPaid();
        BigDecimal maxActualPaid = orderQueryRequest.getMaxActualPaid();
        Date createTimeBegin = orderQueryRequest.getCreateTimeBegin();
        Date createTimeEnd = orderQueryRequest.getCreateTimeEnd();
        Date paidTimeBegin = orderQueryRequest.getPaidTimeBegin();
        Date paidTimeEnd = orderQueryRequest.getPaidTimeEnd();
        String sortField = orderQueryRequest.getSortField();
        String sortOrder = orderQueryRequest.getSortOrder();

        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(orderId), "orderId", orderId);
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotNull(orderStatus), "orderStatus", orderStatus);
        queryWrapper.ge(ObjUtil.isNotNull(minTotalAmount), "totalAmount", minTotalAmount);
        queryWrapper.le(ObjUtil.isNotNull(maxTotalAmount), "totalAmount", maxTotalAmount);
        queryWrapper.ge(ObjUtil.isNotNull(minActualPaid), "actualPaid", minActualPaid);
        queryWrapper.le(ObjUtil.isNotNull(maxActualPaid), "actualPaid", maxActualPaid);
        queryWrapper.ge(ObjUtil.isNotNull(createTimeBegin), "createTime", createTimeBegin);
        queryWrapper.le(ObjUtil.isNotNull(createTimeEnd), "createTime", createTimeEnd);
        queryWrapper.ge(ObjUtil.isNotNull(paidTimeBegin), "paidTime", paidTimeBegin);
        queryWrapper.le(ObjUtil.isNotNull(paidTimeEnd), "paidTime", paidTimeEnd);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);

        return queryWrapper;
    }

    @Override
    public String generateOrderId() {
        return DateUtil.format(new Date(), "yyyyMMddHHmmss") + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
    }

    private Order getOrderByOrderId(String orderId) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("orderId", orderId);
        return this.getOne(queryWrapper);
    }
}