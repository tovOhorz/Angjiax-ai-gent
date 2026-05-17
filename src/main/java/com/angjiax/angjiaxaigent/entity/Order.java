package com.angjiax.angjiaxaigent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单
 * @TableName order
 */
@TableName(value = "`order`")
@Data
public class Order implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 订单号（对外展示用）
     */
    private String orderId;

    /**
     * 用户ID（关联user.id）
     */
    private Long userId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 实付金额
     */
    private BigDecimal actualPaid;

    /**
     * 使用的优惠券ID（关联coupon.id）
     */
    private Long couponId;

    /**
     * 订单状态：1待付款/2待发货/3待收货/4已完成/5已取消
     */
    private Integer orderStatus;

    /**
     * 付款时间
     */
    private Date paidTime;

    /**
     * 收货地址
     */
    private String address;

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

    /**
     * 订单明细列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<OrderItem> orderItems;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}