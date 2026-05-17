package com.angjiax.angjiaxaigent.controller;

import com.angjiax.angjiaxaigent.anotation.AuthCheck;
import com.angjiax.angjiaxaigent.common.BaseResponse;
import com.angjiax.angjiaxaigent.common.DeleteRequest;
import com.angjiax.angjiaxaigent.common.ResultUtils;
import com.angjiax.angjiaxaigent.constant.UserConstant;
import com.angjiax.angjiaxaigent.entity.Coupon;
import com.angjiax.angjiaxaigent.entity.dto.coupon.CouponAddRequest;
import com.angjiax.angjiaxaigent.entity.dto.coupon.CouponQueryRequest;
import com.angjiax.angjiaxaigent.entity.dto.coupon.CouponUpdateRequest;
import com.angjiax.angjiaxaigent.entity.vo.coupon.CouponVO;
import com.angjiax.angjiaxaigent.exception.BusinessException;
import com.angjiax.angjiaxaigent.exception.ErrorCode;
import com.angjiax.angjiaxaigent.exception.ThrowUtils;
import com.angjiax.angjiaxaigent.manager.CacheManager;
import com.angjiax.angjiaxaigent.service.CouponService;
import com.angjiax.angjiaxaigent.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@RequestMapping("/coupon")
@RestController
@Slf4j
public class CouponController {

    @Resource
    private CouponService couponService;

    @Resource
    private UserService userService;

    @Resource
    private CacheManager cacheManager;

    /**
     * 创建优惠券（仅管理员）
     */
    @PostMapping("/add")
    //@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addCoupon(@RequestBody CouponAddRequest couponAddRequest) {
        ThrowUtils.throwIf(couponAddRequest == null, ErrorCode.PARAMS_ERROR);
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(couponAddRequest, coupon);
        boolean result = couponService.save(coupon);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(coupon.getId());
    }

    /**
     * 根据 id 获取优惠券（仅管理员）
     */
    @GetMapping("/get")
    //@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Coupon> getCouponById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        String cacheKey = "coupon:" + id;
        // 查缓存
        Coupon coupon = (Coupon) cacheManager.get(cacheKey);
        if (coupon != null) {
            return ResultUtils.success(coupon);
        }
        coupon = couponService.getById(id);
        // 如果查询不存在的优惠券ID，缓存空值防止缓存穿透
        if (coupon == null) {
            cacheManager.put(cacheKey, "NULL", TimeUnit.MINUTES.toMillis(5));
            ThrowUtils.throwIf(true, ErrorCode.NOT_FOUND_ERROR);
        } else {
            // 缓存真实数据
            long ttl = TimeUnit.HOURS.toMillis(1) + ThreadLocalRandom.current().nextInt(0, 10000);
            cacheManager.put(cacheKey, coupon, TimeUnit.HOURS.toMillis(ttl));
        }
        return ResultUtils.success(coupon);
    }

    /**
     * 根据 id 获取优惠券VO
     */
    @GetMapping("/get/vo")
    public BaseResponse<CouponVO> getCouponVOById(long id) {
        BaseResponse<Coupon> response = getCouponById(id);
        Coupon coupon = response.getData();
        return ResultUtils.success(couponService.getCouponVO(coupon));
    }

    /**
     * 删除优惠券（仅管理员）
     */
    @PostMapping("/delete")
    //@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteCoupon(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = couponService.removeById(deleteRequest.getId());
        // 删除缓存
        cacheManager.remove("coupon:" + deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新优惠券（仅管理员）
     */
    @PostMapping("/update")
    //@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateCoupon(@RequestBody CouponUpdateRequest couponUpdateRequest) {
        if (couponUpdateRequest == null || couponUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(couponUpdateRequest, coupon);
        boolean result = couponService.updateById(coupon);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 清除缓存
        cacheManager.remove("coupon:" + coupon.getId());
        return ResultUtils.success(true);
    }

    /**
     * 分页获取优惠券列表（管理员）
     */
    @PostMapping("/list/page")
    //@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Coupon>> listCouponByPage(@RequestBody CouponQueryRequest couponQueryRequest) {
        ThrowUtils.throwIf(couponQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = couponQueryRequest.getCurrent();
        long pageSize = couponQueryRequest.getPageSize();
        Page<Coupon> couponPage = couponService.page(new Page<>(current, pageSize),
                couponService.getQueryWrapper(couponQueryRequest));
        return ResultUtils.success(couponPage);
    }

    /**
     * 分页获取优惠券VO列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<CouponVO>> listCouponVOByPage(@RequestBody CouponQueryRequest couponQueryRequest) {
        ThrowUtils.throwIf(couponQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = couponQueryRequest.getCurrent();
        long pageSize = couponQueryRequest.getPageSize();
        Page<Coupon> couponPage = couponService.page(new Page<>(current, pageSize),
                couponService.getQueryWrapper(couponQueryRequest));
        Page<CouponVO> couponVOPage = new Page<>(current, pageSize, couponPage.getTotal());
        List<CouponVO> couponVOList = couponService.getCouponVOList(couponPage.getRecords());
        couponVOPage.setRecords(couponVOList);
        return ResultUtils.success(couponVOPage);
    }

    /**
     * 获取可用优惠券列表（前台用户使用）
     */
    @GetMapping("/available")
    public BaseResponse<List<CouponVO>> getAvailableCoupons(@RequestParam(required = false) BigDecimal orderAmount) {
        List<CouponVO> couponList = couponService.getAvailableCoupons(orderAmount);
        return ResultUtils.success(couponList);
    }

    /**
     * 根据金额计算优惠券优惠金额（预览用）
     */
    @PostMapping("/calculate/discount")
    public BaseResponse<BigDecimal> calculateDiscount(@RequestParam Long couponId, @RequestParam BigDecimal amount) {
        ThrowUtils.throwIf(couponId == null || amount == null, ErrorCode.PARAMS_ERROR);
        Coupon coupon = couponService.validateCoupon(couponId, amount);
        BigDecimal discount = couponService.calculateDiscount(coupon, amount);
        return ResultUtils.success(discount);
    }
}