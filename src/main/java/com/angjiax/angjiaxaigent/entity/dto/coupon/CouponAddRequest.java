package com.angjiax.angjiaxaigent.entity.dto.coupon;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class CouponAddRequest implements Serializable {

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠券类型：1满减券/2折扣券/3无门槛券
     */
    private Integer couponType;

    /**
     * 满减条件（满X元可用）
     */
    private BigDecimal conditionAmount;

    /**
     * 优惠金额（减Y元）
     */
    private BigDecimal discountAmount;

    /**
     * 折扣率（如0.85表示85折）
     */
    private BigDecimal discountRate;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 适用商品ID列表（JSON数组）
     */
    private String applicableProducts;

    /**
     * 所属平台
     */
    private String platform;

    /**
     * 总发行量
     */
    private Integer totalQuantity;

    private static final long serialVersionUID = 1L;
}