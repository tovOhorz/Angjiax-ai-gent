package com.angjiax.angjiaxaigent.service;

import com.angjiax.angjiaxaigent.entity.Cart;
import com.angjiax.angjiaxaigent.entity.dto.cart.CartQueryRequest;
import com.angjiax.angjiaxaigent.entity.vo.cart.CartVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author t'h
 * @description 针对表【cart(购物车)】的数据库操作Service
 * @createDate 2026-05-02
 */
public interface CartService extends IService<Cart> {

    /**
     * 添加商品到购物车
     *
     * @param userId
     * @param productId
     * @param quantity
     * @return
     */
    Boolean addToCart(Long userId, Long productId, Integer quantity);

    /**
     * 更新购物车商品数量
     *
     * @param userId
     * @param cartId
     * @param quantity
     * @return
     */
    Boolean updateQuantity(Long userId, Long cartId, Integer quantity);

    /**
     * 切换商品选中状态
     *
     * @param userId
     * @param cartId
     * @param selected
     * @return
     */
    Boolean toggleSelected(Long userId, Long cartId, Integer selected);

    /**
     * 全选/全不选
     *
     * @param userId
     * @param selected
     * @return
     */
    Boolean selectAll(Long userId, Integer selected);

    /**
     * 删除购物车商品
     *
     * @param userId
     * @param cartId
     * @return
     */
    Boolean removeFromCart(Long userId, Long cartId);

    /**
     * 批量删除购物车商品
     *
     * @param userId
     * @param cartIds
     * @return
     */
    Boolean batchRemoveFromCart(Long userId, List<Long> cartIds);

    /**
     * 获取用户购物车列表
     *
     * @param userId
     * @return
     */
    List<CartVO> getUserCart(Long userId);

    /**
     * 获取用户选中的购物车商品
     *
     * @param userId
     * @return
     */
    List<CartVO> getSelectedCartItems(Long userId);

    /**
     * 计算选中商品总价
     *
     * @param userId
     * @return
     */
    BigDecimal calculateSelectedTotal(Long userId);

    /**
     * 清空用户购物车
     *
     * @param userId
     * @return
     */
    Boolean clearCart(Long userId);

    /**
     * 根据ID列表获取购物车商品
     */
    List<CartVO> getCartItemsByIds(Long userId, List<Long> cartItemIds);

    /**
     * 管理员分页查询全部购物车（可按用户、商品筛选）
     */
    Page<CartVO> listCartVOPageAdmin(CartQueryRequest request);
}