package com.angjiax.angjiaxaigent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单明细
 * @TableName order_item
 */
@TableName(value = "order_item")
@Data
public class OrderItem implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 订单号（关联order.orderId）
     */
    private String orderId;

    /**
     * 商品ID（关联product.id）
     */
    private Long productId;

    /**
     * 商品名称（快照）
     */
    private String productName;

    /**
     * 商品图片（快照）
     */
    private String productImage;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 小计
     */
    private BigDecimal totalPrice;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}