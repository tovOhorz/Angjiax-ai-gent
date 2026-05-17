package com.angjiax.angjiaxaigent.service;

import com.angjiax.angjiaxaigent.entity.Coupon;
import com.angjiax.angjiaxaigent.entity.dto.coupon.CouponQueryRequest;
import com.angjiax.angjiaxaigent.entity.vo.coupon.CouponVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author t'h
 * @description 针对表【coupon(优惠券)】的数据库操作Service
 * @createDate 2026-05-02
 */
public interface CouponService extends IService<Coupon> {

    /**
     * 获取优惠券VO
     *
     * @param coupon
     * @return
     */
    CouponVO getCouponVO(Coupon coupon);

    /**
     * 获取可用优惠券列表
     *
     * @param amount 订单金额
     * @return
     */
    List<CouponVO> getAvailableCoupons(BigDecimal amount);

    /**
     * 计算优惠金额
     *
     * @param coupon
     * @param amount 订单金额
     * @return
     */
    BigDecimal calculateDiscount(Coupon coupon, BigDecimal amount);

    /**
     * 校验优惠券是否可用
     *
     * @param couponId
     * @param amount 订单金额
     * @return
     */
    Coupon validateCoupon(Long couponId, BigDecimal amount);

    /**
     * 获取优惠券VO列表
     *
     * @param couponList
     * @return
     */
    List<CouponVO> getCouponVOList(List<Coupon> couponList);

    /**
     * 获取查询条件
     *
     * @param couponQueryRequest
     * @return
     */
    QueryWrapper<Coupon> getQueryWrapper(CouponQueryRequest couponQueryRequest);
}