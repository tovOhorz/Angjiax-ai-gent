package com.angjiax.angjiaxaigent.service;

import com.angjiax.angjiaxaigent.entity.UserCoupon;
import com.angjiax.angjiaxaigent.entity.vo.usercoupon.UserCouponVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author t'h
 * @description 针对表【user_coupon(用户优惠券记录)】的数据库操作Service
 * @createDate 2026-05-02
 */
public interface UserCouponService extends IService<UserCoupon> {

    /**
     * 领取优惠券
     *
     * @param userId
     * @param couponId
     * @return
     */
    Boolean receiveCoupon(Long userId, Long couponId);

    /**
     * 获取用户所有优惠券
     *
     * @param userId
     * @return
     */
    List<UserCouponVO> getUserCoupons(Long userId);

    /**
     * 获取用户可用的优惠券
     *
     * @param userId
     * @param orderAmount 订单金额
     * @return
     */
    List<UserCouponVO> getAvailableUserCoupons(Long userId, java.math.BigDecimal orderAmount);

    /**
     * 获取用户有效的优惠券（未使用、未过期）
     *
     * @param userId
     * @param couponId
     * @return
     */
    UserCoupon getValidUserCoupon(Long userId, Long couponId);

    /**
     * 标记优惠券已使用
     *
     * @param userId
     * @param couponId
     * @param orderId
     * @return
     */
    Boolean markUsed(Long userId, Long couponId, String orderId);

    /**
     * 恢复优惠券（取消订单时）
     *
     * @param userId
     * @param couponId
     * @param orderId
     * @return
     */
    Boolean restoreCoupon(Long userId, Long couponId, String orderId);
}