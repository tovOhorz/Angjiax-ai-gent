package com.angjiax.angjiaxaigent.entity.dto.coupon;

import com.angjiax.angjiaxaigent.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class CouponQueryRequest extends PageRequest implements Serializable {

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
     * 所属平台
     */
    private String platform;

    /**
     * 状态：1有效/0失效
     */
    private Integer status;

    /**
     * 开始时间起
     */
    private Date startTimeBegin;

    /**
     * 开始时间止
     */
    private Date startTimeEnd;

    /**
     * 结束时间起
     */
    private Date endTimeBegin;

    /**
     * 结束时间止
     */
    private Date endTimeEnd;

    private static final long serialVersionUID = 1L;
}