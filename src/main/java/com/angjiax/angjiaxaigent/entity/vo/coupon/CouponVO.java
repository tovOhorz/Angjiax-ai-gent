package com.angjiax.angjiaxaigent.entity.vo.coupon;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class CouponVO implements Serializable {

    /**
     * id
     */
    private Long id;

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
     * 折扣率
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
     * 所属平台
     */
    private String platform;

    /**
     * 状态：1有效/0失效
     */
    private Integer status;

    /**
     * 优惠券描述（前端展示用）
     */
    private String description;

    private static final long serialVersionUID = 1L;
}