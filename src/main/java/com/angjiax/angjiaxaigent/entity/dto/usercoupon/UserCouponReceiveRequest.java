package com.angjiax.angjiaxaigent.entity.dto.usercoupon;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserCouponReceiveRequest implements Serializable {

    /**
     * 优惠券ID
     */
    private Long couponId;

    private static final long serialVersionUID = 1L;
}