package com.angjiax.angjiaxaigent.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 购物车
 * @TableName cart
 */
@TableName(value ="cart")
@Data
public class Cart implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID（关联user.id）
     */
    private Long userId;

    /**
     * 商品ID（关联product.id）
     */
    private Long productId;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 是否选中：1是/0否
     */
    private Integer selected;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}