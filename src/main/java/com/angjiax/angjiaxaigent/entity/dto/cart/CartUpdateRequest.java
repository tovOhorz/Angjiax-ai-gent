package com.angjiax.angjiaxaigent.entity.dto.cart;

import lombok.Data;

import java.io.Serializable;

@Data
public class CartUpdateRequest implements Serializable {

    /**
     * 购物车项ID
     */
    private Long id;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 是否选中：1是/0否
     */
    private Integer selected;

    private static final long serialVersionUID = 1L;
}