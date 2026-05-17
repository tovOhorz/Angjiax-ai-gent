package com.angjiax.angjiaxaigent.entity.vo.usercoupon;

import com.angjiax.angjiaxaigent.entity.vo.coupon.CouponVO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserCouponVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 优惠券信息
     */
    private CouponVO coupon;

    /**
     * 状态：1未使用/2已使用/3已过期
     */
    private Integer status;

    /**
     * 状态文本
     */
    private String statusText;

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

    private static final long serialVersionUID = 1L;
}