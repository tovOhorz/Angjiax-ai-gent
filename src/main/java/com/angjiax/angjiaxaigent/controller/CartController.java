package com.angjiax.angjiaxaigent.controller;

import com.angjiax.angjiaxaigent.common.BaseResponse;
import com.angjiax.angjiaxaigent.common.ResultUtils;
import com.angjiax.angjiaxaigent.entity.dto.cart.CartAddRequest;
import com.angjiax.angjiaxaigent.entity.dto.cart.CartQueryRequest;
import com.angjiax.angjiaxaigent.entity.dto.cart.CartUpdateRequest;
import com.angjiax.angjiaxaigent.entity.vo.cart.CartVO;
import com.angjiax.angjiaxaigent.exception.ThrowUtils;
import com.angjiax.angjiaxaigent.service.CartService;
import com.angjiax.angjiaxaigent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.List;

@RequestMapping("/cart")
@RestController
@Slf4j
public class CartController {

    @Resource
    private CartService cartService;

    @Resource
    private UserService userService;

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addToCart(@RequestBody CartAddRequest cartAddRequest, HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        ThrowUtils.throwIf(cartAddRequest == null, com.angjiax.angjiaxaigent.exception.ErrorCode.PARAMS_ERROR);
        Boolean result = cartService.addToCart(userId, cartAddRequest.getProductId(), cartAddRequest.getQuantity());
        return ResultUtils.success(result);
    }

    /**
     * 更新购物车商品数量
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateQuantity(@RequestBody CartUpdateRequest cartUpdateRequest, HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        ThrowUtils.throwIf(cartUpdateRequest == null, com.angjiax.angjiaxaigent.exception.ErrorCode.PARAMS_ERROR);
        Boolean result = cartService.updateQuantity(userId, cartUpdateRequest.getId(), cartUpdateRequest.getQuantity());
        return ResultUtils.success(result);
    }

    /**
     * 切换商品选中状态
     */
    @PostMapping("/toggle")
    public BaseResponse<Boolean> toggleSelected(@RequestBody CartUpdateRequest cartUpdateRequest, HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        ThrowUtils.throwIf(cartUpdateRequest == null, com.angjiax.angjiaxaigent.exception.ErrorCode.PARAMS_ERROR);
        Boolean result = cartService.toggleSelected(userId, cartUpdateRequest.getId(), cartUpdateRequest.getSelected());
        return ResultUtils.success(result);
    }

    /**
     * 全选/全不选
     */
    @PostMapping("/select/all")
    public BaseResponse<Boolean> selectAll(@RequestParam Integer selected, HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        Boolean result = cartService.selectAll(userId, selected);
        return ResultUtils.success(result);
    }

    /**
     * 删除购物车商品
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> removeFromCart(@RequestParam Long cartId, HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        Boolean result = cartService.removeFromCart(userId, cartId);
        return ResultUtils.success(result);
    }

    /**
     * 批量删除购物车商品
     */
    @PostMapping("/delete/batch")
    public BaseResponse<Boolean> batchRemoveFromCart(@RequestBody List<Long> cartIds, HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        Boolean result = cartService.batchRemoveFromCart(userId, cartIds);
        return ResultUtils.success(result);
    }

    /**
     * 获取购物车列表
     */
    @GetMapping("/list")
    public BaseResponse<List<CartVO>> getCartList(HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        List<CartVO> cartList = cartService.getUserCart(userId);
        return ResultUtils.success(cartList);
    }

    /**
     * 管理员分页查询购物车（全用户）
     */
    @PostMapping("/list/page/vo/admin")
    public BaseResponse<Page<CartVO>> listCartVOByPageAdmin(@RequestBody CartQueryRequest cartQueryRequest) {
        ThrowUtils.throwIf(cartQueryRequest == null, com.angjiax.angjiaxaigent.exception.ErrorCode.PARAMS_ERROR);
        Page<CartVO> page = cartService.listCartVOPageAdmin(cartQueryRequest);
        return ResultUtils.success(page);
    }

    /**
     * 获取选中商品总价
     */
    @GetMapping("/total")
    public BaseResponse<BigDecimal> getSelectedTotal(HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        BigDecimal total = cartService.calculateSelectedTotal(userId);
        return ResultUtils.success(total);
    }
}