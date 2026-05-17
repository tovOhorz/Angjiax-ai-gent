package com.angjiax.angjiaxaigent.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.angjiax.angjiaxaigent.entity.Coupon;
import com.angjiax.angjiaxaigent.entity.UserCoupon;
import com.angjiax.angjiaxaigent.entity.vo.coupon.CouponVO;
import com.angjiax.angjiaxaigent.entity.vo.usercoupon.UserCouponVO;
import com.angjiax.angjiaxaigent.exception.BusinessException;
import com.angjiax.angjiaxaigent.exception.ErrorCode;
import com.angjiax.angjiaxaigent.mapper.UserCouponMapper;
import com.angjiax.angjiaxaigent.service.CouponService;
import com.angjiax.angjiaxaigent.service.UserCouponService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author t'h
 * @description 针对表【user_coupon(用户优惠券记录)】的数据库操作Service实现
 * @createDate 2026-05-02
 */
@Service
@Slf4j
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon>
        implements UserCouponService {

    @Autowired
    private CouponService couponService;

    @Override
    public Boolean receiveCoupon(Long userId, Long couponId) {
        if (userId == null || couponId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 检查优惠券是否存在且有效
        Coupon coupon = couponService.getById(couponId);
        if (coupon == null || coupon.getStatus() != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "优惠券不存在或已失效");
        }

        // 检查时间
        Date now = new Date();
        if (now.before(coupon.getStartTime()) || now.after(coupon.getEndTime())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券不在领取时间内");
        }

        // 检查库存
        if (coupon.getTotalQuantity() != null && coupon.getUsedQuantity() >= coupon.getTotalQuantity()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券已领完");
        }

        // 检查是否已领取
        QueryWrapper<UserCoupon> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("couponId", couponId);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "您已领取过此优惠券");
        }

        // 领取优惠券
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(1); // 未使用
        userCoupon.setReceivedAt(now);

        // 更新优惠券已使用量
        coupon.setUsedQuantity(coupon.getUsedQuantity() + 1);
        couponService.updateById(coupon);

        boolean saved = this.save(userCoupon);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "领取失败");
        }

        log.info("用户领取优惠券成功，userId：{}，couponId：{}", userId, couponId);
        return true;
    }

    @Override
    public List<UserCouponVO> getUserCoupons(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<UserCoupon> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.orderByDesc("receivedAt");
        List<UserCoupon> userCoupons = this.list(queryWrapper);

        if (CollUtil.isEmpty(userCoupons)) {
            return List.of();
        }

        return userCoupons.stream().map(uc -> {
            UserCouponVO vo = new UserCouponVO();
            BeanUtils.copyProperties(uc, vo);

            // 设置状态文本
            String statusText;
            if (uc.getStatus() == 1) {
                statusText = "未使用";
            } else if (uc.getStatus() == 2) {
                statusText = "已使用";
            } else {
                statusText = "已过期";
            }
            vo.setStatusText(statusText);

            // 获取优惠券详情
            Coupon coupon = couponService.getById(uc.getCouponId());
            if (coupon != null) {
                vo.setCoupon(couponService.getCouponVO(coupon));
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<UserCouponVO> getAvailableUserCoupons(Long userId, BigDecimal orderAmount) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        List<UserCouponVO> allCoupons = getUserCoupons(userId);
        Date now = new Date();

        return allCoupons.stream()
                .filter(vo -> vo.getStatus() == 1) // 未使用
                .filter(vo -> {
                    CouponVO couponVO = vo.getCoupon();
                    if (couponVO == null) return false;
                    // 检查有效期（CouponVO 中也有这些字段）
                    if (now.before(couponVO.getStartTime()) || now.after(couponVO.getEndTime())) {
                        return false;
                    }
                    // 满减券检查金额
                    if (couponVO.getCouponType() == 1 && orderAmount != null) {
                        return orderAmount.compareTo(couponVO.getConditionAmount()) >= 0;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserCoupon getValidUserCoupon(Long userId, Long couponId) {
        if (userId == null || couponId == null) {
            return null;
        }

        QueryWrapper<UserCoupon> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("couponId", couponId);
        queryWrapper.eq("status", 1); // 未使用

        return this.getOne(queryWrapper);
    }

    @Override
    public Boolean markUsed(Long userId, Long couponId, String orderId) {
        UserCoupon userCoupon = getValidUserCoupon(userId, couponId);
        if (userCoupon == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "优惠券不存在或已使用");
        }

        userCoupon.setStatus(2); // 已使用
        userCoupon.setUsedAt(new Date());
        userCoupon.setOrderId(orderId);

        boolean updated = this.updateById(userCoupon);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "使用优惠券失败");
        }

        log.info("优惠券已使用，userId：{}，couponId：{}，orderId：{}", userId, couponId, orderId);
        return true;
    }

    @Override
    public Boolean restoreCoupon(Long userId, Long couponId, String orderId) {
        QueryWrapper<UserCoupon> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("couponId", couponId);
        queryWrapper.eq("orderId", orderId);
        queryWrapper.eq("status", 2); // 已使用

        UserCoupon userCoupon = this.getOne(queryWrapper);
        if (userCoupon == null) {
            log.warn("恢复优惠券失败，未找到已使用的优惠券记录，userId：{}，couponId：{}，orderId：{}", userId, couponId, orderId);
            return false;
        }

        userCoupon.setStatus(1); // 未使用
        userCoupon.setUsedAt(null);
        userCoupon.setOrderId(null);

        boolean updated = this.updateById(userCoupon);
        if (updated) {
            log.info("优惠券已恢复，userId：{}，couponId：{}", userId, couponId);
        }

        return updated;
    }
}