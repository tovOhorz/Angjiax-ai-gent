package com.angjiax.angjiaxaigent.controller;

import com.angjiax.angjiaxaigent.anotation.AuthCheck;
import com.angjiax.angjiaxaigent.common.BaseResponse;
import com.angjiax.angjiaxaigent.common.ResultUtils;
import com.angjiax.angjiaxaigent.constant.UserConstant;
import com.angjiax.angjiaxaigent.entity.Order;
import com.angjiax.angjiaxaigent.entity.dto.order.OrderAddRequest;
import com.angjiax.angjiaxaigent.entity.dto.order.OrderFromCartRequest;
import com.angjiax.angjiaxaigent.entity.dto.order.OrderQueryRequest;
import com.angjiax.angjiaxaigent.entity.vo.order.OrderVO;
import com.angjiax.angjiaxaigent.exception.ThrowUtils;
import com.angjiax.angjiaxaigent.service.OrderService;
import com.angjiax.angjiaxaigent.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/order")
@RestController
@Slf4j
public class OrderController {

    @Resource
    private OrderService orderService;

    @Resource
    private UserService userService;

    /**
     * 单商品创建订单
     */
    @PostMapping("/create")
    public BaseResponse<Order> createOrder(@RequestBody OrderAddRequest orderAddRequest, HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        ThrowUtils.throwIf(orderAddRequest == null, com.angjiax.angjiaxaigent.exception.ErrorCode.PARAMS_ERROR);
        Order order = orderService.createOrder(userId, orderAddRequest);
        return ResultUtils.success(order);
    }

    /**
     * 从购物车创建订单（支持多商品）
     */
    @PostMapping("/createFromCart")
    public BaseResponse<Order> createOrderFromCart(@RequestBody OrderFromCartRequest request, HttpServletRequest httpRequest) {
        Long userId = userService.getLoginUser(httpRequest).getId();
        ThrowUtils.throwIf(request == null, com.angjiax.angjiaxaigent.exception.ErrorCode.PARAMS_ERROR);
        Order order = orderService.createOrderFromCart(userId, request.getCartItemIds(), request.getCouponId(), request.getAddress());
        return ResultUtils.success(order);
    }

    /**
     * 支付订单
     */
    @PostMapping("/pay")
    public BaseResponse<Boolean> payOrder(@RequestParam String orderId, HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        Boolean result = orderService.payOrder(userId, orderId);
        return ResultUtils.success(result);
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel")
    public BaseResponse<Boolean> cancelOrder(@RequestParam String orderId, HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        Boolean result = orderService.cancelOrder(userId, orderId);
        return ResultUtils.success(result);
    }

    /**
     * 发货（仅管理员）
     */
    @PostMapping("/ship")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> shipOrder(@RequestParam String orderId) {
        Boolean result = orderService.shipOrder(orderId);
        return ResultUtils.success(result);
    }

    /**
     * 确认收货
     */
    @PostMapping("/confirm")
    public BaseResponse<Boolean> confirmOrder(@RequestParam String orderId, HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        Boolean result = orderService.confirmOrder(userId, orderId);
        return ResultUtils.success(result);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/detail")
    public BaseResponse<OrderVO> getOrderDetail(@RequestParam String orderId, HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        Order order = orderService.getOne(new QueryWrapper<Order>()
                .eq("orderId", orderId)
                .eq("userId", userId));
        ThrowUtils.throwIf(order == null, com.angjiax.angjiaxaigent.exception.ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(orderService.getOrderVO(order));
    }

    /**
     * 分页获取用户订单列表
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<OrderVO>> getUserOrders(@RequestBody OrderQueryRequest orderQueryRequest, HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        Page<OrderVO> orderVOPage = orderService.getOrderVOPage(userId, orderQueryRequest);
        return ResultUtils.success(orderVOPage);
    }

    /**
     * 分页获取订单列表（仅管理员）
     */
    @PostMapping("/list/page/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Order>> listOrderByPage(@RequestBody OrderQueryRequest orderQueryRequest) {
        ThrowUtils.throwIf(orderQueryRequest == null, com.angjiax.angjiaxaigent.exception.ErrorCode.PARAMS_ERROR);
        long current = orderQueryRequest.getCurrent();
        long pageSize = orderQueryRequest.getPageSize();
        Page<Order> orderPage = orderService.page(new Page<>(current, pageSize),
                orderService.getQueryWrapper(orderQueryRequest));
        return ResultUtils.success(orderPage);
    }

    /**
     * 分页获取订单VO列表（仅管理员）
     */
    @PostMapping("/list/page/vo/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<OrderVO>> listOrderVOByPage(@RequestBody OrderQueryRequest orderQueryRequest) {
        ThrowUtils.throwIf(orderQueryRequest == null, com.angjiax.angjiaxaigent.exception.ErrorCode.PARAMS_ERROR);
        long current = orderQueryRequest.getCurrent();
        long pageSize = orderQueryRequest.getPageSize();
        Page<Order> orderPage = orderService.page(new Page<>(current, pageSize),
                orderService.getQueryWrapper(orderQueryRequest));
        Page<OrderVO> orderVOPage = new Page<>(current, pageSize, orderPage.getTotal());
        List<OrderVO> orderVOList = orderPage.getRecords().stream()
                .map(orderService::getOrderVO)
                .collect(Collectors.toList());
        orderVOPage.setRecords(orderVOList);
        return ResultUtils.success(orderVOPage);
    }
}