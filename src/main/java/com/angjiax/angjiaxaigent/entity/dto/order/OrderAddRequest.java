package com.angjiax.angjiaxaigent.entity.dto.order;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderAddRequest implements Serializable {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 优惠券ID（可选）
     */
    private Long couponId;

    /**
     * 收货地址
     */
    private String address;

    private static final long serialVersionUID = 1L;
}