package com.angjiax.angjiaxaigent.entity.dto.order;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OrderFromCartRequest implements Serializable {

    /**
     * 购物车项ID列表（选中结算的商品）
     */
    private List<Long> cartItemIds;

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