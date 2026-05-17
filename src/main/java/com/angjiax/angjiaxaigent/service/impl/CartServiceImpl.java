package com.angjiax.angjiaxaigent.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.angjiax.angjiaxaigent.entity.Cart;
import com.angjiax.angjiaxaigent.entity.Product;
import com.angjiax.angjiaxaigent.entity.dto.cart.CartQueryRequest;
import com.angjiax.angjiaxaigent.entity.vo.cart.CartVO;
import com.angjiax.angjiaxaigent.exception.BusinessException;
import com.angjiax.angjiaxaigent.exception.ErrorCode;
import com.angjiax.angjiaxaigent.exception.ThrowUtils;
import com.angjiax.angjiaxaigent.mapper.CartMapper;
import com.angjiax.angjiaxaigent.service.CartService;
import com.angjiax.angjiaxaigent.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author t'h
 * @description 针对表【cart(购物车)】的数据库操作Service实现
 * @createDate 2026-05-02
 */
@Service
@Slf4j
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart>
        implements CartService {

    @Autowired
    private ProductService productService;

    @Override
    public Boolean addToCart(Long userId, Long productId, Integer quantity) {
        // 参数校验
        if (userId == null || productId == null || quantity == null || quantity <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 检查商品是否存在且上架
        Product product = productService.getById(productId);
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品不存在或已下架");
        }

        // 检查库存
        if (product.getStock() < quantity) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "库存不足");
        }

        // 查询购物车是否已有该商品
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("productId", productId);
        Cart existCart = this.getOne(queryWrapper);

        if (existCart != null) {
            // 已存在，增加数量
            existCart.setQuantity(existCart.getQuantity() + quantity);
            return this.updateById(existCart);
        } else {
            // 不存在，新增
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cart.setSelected(1);
            return this.save(cart);
        }
    }

    @Override
    public Boolean updateQuantity(Long userId, Long cartId, Integer quantity) {
        if (userId == null || cartId == null || quantity == null || quantity <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Cart cart = this.getById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "购物车商品不存在");
        }

        // 检查库存
        Product product = productService.getById(cart.getProductId());
        if (product.getStock() < quantity) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "库存不足");
        }

        cart.setQuantity(quantity);
        return this.updateById(cart);
    }

    @Override
    public Boolean toggleSelected(Long userId, Long cartId, Integer selected) {
        if (userId == null || cartId == null || selected == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Cart cart = this.getById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "购物车商品不存在");
        }

        cart.setSelected(selected);
        return this.updateById(cart);
    }

    @Override
    public Boolean selectAll(Long userId, Integer selected) {
        if (userId == null || selected == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        List<Cart> cartList = this.list(queryWrapper);

        if (CollUtil.isEmpty(cartList)) {
            return true;
        }

        for (Cart cart : cartList) {
            cart.setSelected(selected);
        }
        return this.updateBatchById(cartList);
    }

    @Override
    public Boolean removeFromCart(Long userId, Long cartId) {
        if (userId == null || cartId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", cartId);
        queryWrapper.eq("userId", userId);
        return this.remove(queryWrapper);
    }

    @Override
    public Boolean batchRemoveFromCart(Long userId, List<Long> cartIds) {
        if (userId == null || CollUtil.isEmpty(cartIds)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.in("id", cartIds);
        return this.remove(queryWrapper);
    }

    @Override
    public List<CartVO> getUserCart(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.orderByDesc("createTime");
        List<Cart> cartList = this.list(queryWrapper);

        if (CollUtil.isEmpty(cartList)) {
            return new ArrayList<>();
        }

        return cartList.stream().map(cart -> {
            CartVO cartVO = new CartVO();
            BeanUtils.copyProperties(cart, cartVO);

            Product product = productService.getById(cart.getProductId());
            if (product != null) {
                cartVO.setProductName(product.getProductName());
                cartVO.setImageUrl(product.getImageUrl());
                cartVO.setCurrentPrice(product.getCurrentPrice());
                cartVO.setStock(product.getStock());
                cartVO.setStatus(product.getStatus());
                // 计算小计
                BigDecimal subtotal = product.getCurrentPrice().multiply(new BigDecimal(cart.getQuantity()));
                cartVO.setSubtotal(subtotal);
            }
            return cartVO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<CartVO> getSelectedCartItems(Long userId) {
        List<CartVO> allCart = getUserCart(userId);
        return allCart.stream()
                .filter(cart -> cart.getSelected() == 1 && cart.getStatus() == 1)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculateSelectedTotal(Long userId) {
        List<CartVO> selectedItems = getSelectedCartItems(userId);
        if (CollUtil.isEmpty(selectedItems)) {
            return BigDecimal.ZERO;
        }
        return selectedItems.stream()
                .map(CartVO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Boolean clearCart(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        return this.remove(queryWrapper);
    }

    @Override
    public List<CartVO> getCartItemsByIds(Long userId, List<Long> cartItemIds) {
        if (userId == null || CollUtil.isEmpty(cartItemIds)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.in("id", cartItemIds);
        List<Cart> cartList = this.list(queryWrapper);

        if (CollUtil.isEmpty(cartList)) {
            return new ArrayList<>();
        }

        return cartList.stream().map(cart -> {
            CartVO cartVO = new CartVO();
            BeanUtils.copyProperties(cart, cartVO);

            Product product = productService.getById(cart.getProductId());
            if (product != null) {
                cartVO.setProductName(product.getProductName());
                cartVO.setImageUrl(product.getImageUrl());
                cartVO.setCurrentPrice(product.getCurrentPrice());
                cartVO.setStock(product.getStock());
                cartVO.setStatus(product.getStatus());
                BigDecimal subtotal = product.getCurrentPrice().multiply(new BigDecimal(cart.getQuantity()));
                cartVO.setSubtotal(subtotal);
            }
            return cartVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<CartVO> listCartVOPageAdmin(CartQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        long current = request.getCurrent();
        long pageSize = request.getPageSize();
        Page<Cart> page = new Page<>(current, pageSize);
        QueryWrapper<Cart> qw = new QueryWrapper<>();
        if (request.getUserId() != null) {
            qw.eq("userId", request.getUserId());
        }
        if (request.getProductId() != null) {
            qw.eq("productId", request.getProductId());
        }
        qw.orderByDesc("createTime");
        Page<Cart> cartPage = this.page(page, qw);
        Page<CartVO> voPage = new Page<>(current, pageSize, cartPage.getTotal());
        List<CartVO> vos = cartPage.getRecords().stream().map(cart -> {
            CartVO cartVO = new CartVO();
            BeanUtils.copyProperties(cart, cartVO);
            cartVO.setUserId(cart.getUserId());
            Product product = productService.getById(cart.getProductId());
            if (product != null) {
                cartVO.setProductName(product.getProductName());
                cartVO.setImageUrl(product.getImageUrl());
                cartVO.setCurrentPrice(product.getCurrentPrice());
                cartVO.setStock(product.getStock());
                cartVO.setStatus(product.getStatus());
                BigDecimal subtotal = product.getCurrentPrice().multiply(new BigDecimal(cart.getQuantity()));
                cartVO.setSubtotal(subtotal);
            }
            return cartVO;
        }).collect(Collectors.toList());
        voPage.setRecords(vos);
        return voPage;
    }


}