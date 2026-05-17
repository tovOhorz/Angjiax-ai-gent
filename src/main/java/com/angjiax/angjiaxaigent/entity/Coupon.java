package com.angjiax.angjiaxaigent.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 优惠券
 * @TableName coupon
 */
@TableName(value ="coupon")
@Data
public class Coupon implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠券类型：1店铺券/2平台券/3会员券
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
     * 适用商品ID列表（JSON数组，存商品的id）
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

    /**
     * 已使用量
     */
    private Integer usedQuantity;

    /**
     * 状态：1有效/0失效
     */
    private Integer status;

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