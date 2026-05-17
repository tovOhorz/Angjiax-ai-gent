package com.angjiax.angjiaxaigent.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户优惠券记录
 * @TableName user_coupon
 */
@TableName(value ="user_coupon")
@Data
public class UserCoupon implements Serializable {
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
     * 优惠券ID（关联coupon.id）
     */
    private Long couponId;

    /**
     * 状态：1未使用/2已使用/3已过期
     */
    private Integer status;

    /**
     * 领取时间
     */
    private Date receivedAt;

    /**
     * 使用时间
     */
    private Date usedAt;

    /**
     * 关联订单ID
     */
    private String orderId;

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