package com.angjiax.angjiaxaigent.service;

import com.angjiax.angjiaxaigent.entity.Order;
import com.angjiax.angjiaxaigent.entity.dto.order.OrderAddRequest;
import com.angjiax.angjiaxaigent.entity.dto.order.OrderQueryRequest;
import com.angjiax.angjiaxaigent.entity.vo.order.OrderVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author t'h
 * @description 针对表【order(订单)】的数据库操作Service
 */
public interface OrderService extends IService<Order> {

    /**
     * 单商品创建订单
     */
    Order createOrder(Long userId, OrderAddRequest orderAddRequest);

    /**
     * 从购物车创建订单（支持多商品）
     */
    Order createOrderFromCart(Long userId, List<Long> cartItemIds, Long couponId, String address);

    /**
     * 支付订单
     */
    Boolean payOrder(Long userId, String orderId);

    /**
     * 取消订单
     */
    Boolean cancelOrder(Long userId, String orderId);

    /**
     * 发货（管理员）
     */
    Boolean shipOrder(String orderId);

    /**
     * 确认收货
     */
    Boolean confirmOrder(Long userId, String orderId);

    /**
     * 获取订单VO
     */
    OrderVO getOrderVO(Order order);

    /**
     * 获取订单VO分页
     */
    Page<OrderVO> getOrderVOPage(Long userId, OrderQueryRequest orderQueryRequest);

    /**
     * 获取查询条件
     */
    QueryWrapper<Order> getQueryWrapper(OrderQueryRequest orderQueryRequest);

    /**
     * 生成订单号
     */
    String generateOrderId();
}