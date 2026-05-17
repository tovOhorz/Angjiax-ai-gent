package com.angjiax.angjiaxaigent.entity.vo.cart;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CartVO implements Serializable {

    /**
     * 购物车项ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String imageUrl;

    /**
     * 商品价格
     */
    private BigDecimal currentPrice;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 小计
     */
    private BigDecimal subtotal;

    /**
     * 是否选中：1是/0否
     */
    private Integer selected;

    /**
     * 库存（用于校验）
     */
    private Integer stock;

    /**
     * 商品状态：1上架/0下架
     */
    private Integer status;

    /**
     * 所属用户 ID（管理员分页列表时填充）
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}