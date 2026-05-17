package com.angjiax.angjiaxaigent.controller;

import com.angjiax.angjiaxaigent.common.BaseResponse;
import com.angjiax.angjiaxaigent.common.ResultUtils;
import com.angjiax.angjiaxaigent.entity.dto.usercoupon.UserCouponReceiveRequest;
import com.angjiax.angjiaxaigent.entity.vo.usercoupon.UserCouponVO;
import com.angjiax.angjiaxaigent.exception.ThrowUtils;
import com.angjiax.angjiaxaigent.service.UserCouponService;
import com.angjiax.angjiaxaigent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RequestMapping("/user/coupon")
@RestController
@Slf4j
public class UserCouponController {

    @Resource
    private UserCouponService userCouponService;

    @Resource
    private UserService userService;

    /**
     * 领取优惠券
     */
    @PostMapping("/receive")
    public BaseResponse<Boolean> receiveCoupon(@RequestBody UserCouponReceiveRequest request, HttpServletRequest httpRequest) {
        Long userId = userService.getLoginUser(httpRequest).getId();
        ThrowUtils.throwIf(request == null, com.angjiax.angjiaxaigent.exception.ErrorCode.PARAMS_ERROR);
        Boolean result = userCouponService.receiveCoupon(userId, request.getCouponId());
        return ResultUtils.success(result);
    }

    /**
     * 获取我的优惠券列表
     */
    @GetMapping("/list")
    public BaseResponse<List<UserCouponVO>> getUserCoupons(HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        List<UserCouponVO> couponList = userCouponService.getUserCoupons(userId);
        return ResultUtils.success(couponList);
    }

    /**
     * 获取可用优惠券列表（下单时使用）
     */
    @GetMapping("/available")
    public BaseResponse<List<UserCouponVO>> getAvailableCoupons(@RequestParam(required = false) BigDecimal orderAmount, HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        List<UserCouponVO> couponList = userCouponService.getAvailableUserCoupons(userId, orderAmount);
        return ResultUtils.success(couponList);
    }
}