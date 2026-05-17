package com.angjiax.angjiaxaigent.entity.dto.cart;

import com.angjiax.angjiaxaigent.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 管理员分页查询购物车
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CartQueryRequest extends PageRequest implements Serializable {

    /**
     * 用户 ID（可选，筛选某用户的购物车）
     */
    private Long userId;

    /**
     * 商品 ID（可选）
     */
    private Long productId;

    private static final long serialVersionUID = 1L;
}
