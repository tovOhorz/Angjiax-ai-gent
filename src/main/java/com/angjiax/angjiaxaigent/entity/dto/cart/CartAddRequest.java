package com.angjiax.angjiaxaigent.entity.dto.cart;

import lombok.Data;

import java.io.Serializable;

@Data
public class CartAddRequest implements Serializable {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 数量
     */
    private Integer quantity;

    private static final long serialVersionUID = 1L;
}