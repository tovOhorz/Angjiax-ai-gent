package com.angjiax.angjiaxaigent.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.angjiax.angjiaxaigent.entity.Coupon;
import com.angjiax.angjiaxaigent.entity.dto.coupon.CouponQueryRequest;
import com.angjiax.angjiaxaigent.entity.vo.coupon.CouponVO;
import com.angjiax.angjiaxaigent.exception.BusinessException;
import com.angjiax.angjiaxaigent.exception.ErrorCode;
import com.angjiax.angjiaxaigent.mapper.CouponMapper;
import com.angjiax.angjiaxaigent.service.CouponService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author t'h
 * @description 针对表【coupon(优惠券)】的数据库操作Service实现
 * @createDate 2026-05-02
 */
@Service
@Slf4j
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon>
        implements CouponService {

    @Override
    public CouponVO getCouponVO(Coupon coupon) {
        if (coupon == null) {
            return null;
        }
        CouponVO couponVO = new CouponVO();
        BeanUtils.copyProperties(coupon, couponVO);

        // 构建描述信息
        StringBuilder desc = new StringBuilder();
        if (coupon.getCouponType() == 1) {
            // 满减券
            desc.append("满").append(coupon.getConditionAmount()).append("减").append(coupon.getDiscountAmount());
        } else if (coupon.getCouponType() == 2) {
            // 折扣券
            int rate = coupon.getDiscountRate().multiply(new BigDecimal(100)).intValue();
            desc.append(rate).append("折");
        } else if (coupon.getCouponType() == 3) {
            // 无门槛券
            desc.append("直减").append(coupon.getDiscountAmount()).append("元");
        }
        couponVO.setDescription(desc.toString());

        return couponVO;
    }

    @Override
    public List<CouponVO> getAvailableCoupons(BigDecimal amount) {
        Date now = new Date();
        QueryWrapper<Coupon> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        queryWrapper.le("startTime", now);
        queryWrapper.ge("endTime", now);
        // 满减券需要满足条件
        queryWrapper.and(wrapper -> wrapper
                .ne("couponType", 1)
                .or(w -> w.eq("couponType", 1).le("conditionAmount", amount))
        );

        List<Coupon> couponList = this.list(queryWrapper);
        if (CollUtil.isEmpty(couponList)) {
            return List.of();
        }

        return couponList.stream().map(this::getCouponVO).collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal amount) {
        if (coupon == null || amount == null) {
            return BigDecimal.ZERO;
        }

        // 满减券
        if (coupon.getCouponType() == 1) {
            if (amount.compareTo(coupon.getConditionAmount()) >= 0) {
                return coupon.getDiscountAmount();
            }
            return BigDecimal.ZERO;
        }

        // 折扣券
        if (coupon.getCouponType() == 2) {
            BigDecimal discount = amount.multiply(BigDecimal.ONE.subtract(coupon.getDiscountRate()));
            return discount.setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        // 无门槛券
        if (coupon.getCouponType() == 3) {
            return coupon.getDiscountAmount();
        }

        return BigDecimal.ZERO;
    }

    @Override
    public Coupon validateCoupon(Long couponId, BigDecimal amount) {
        if (couponId == null) {
            return null;
        }

        Coupon coupon = this.getById(couponId);
        if (coupon == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "优惠券不存在");
        }

        // 检查状态
        if (coupon.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券已失效");
        }

        // 检查时间
        Date now = new Date();
        if (now.before(coupon.getStartTime()) || now.after(coupon.getEndTime())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券不在有效期内");
        }

        // 检查库存
        if (coupon.getTotalQuantity() != null && coupon.getUsedQuantity() >= coupon.getTotalQuantity()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券已领完");
        }

        // 满减券检查金额
        if (coupon.getCouponType() == 1 && amount.compareTo(coupon.getConditionAmount()) < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,
                    "订单金额未满" + coupon.getConditionAmount() + "元，无法使用此优惠券");
        }

        return coupon;
    }

    @Override
    public List<CouponVO> getCouponVOList(List<Coupon> couponList) {
        if (CollUtil.isEmpty(couponList)) {
            return new ArrayList<>();
        }
        return couponList.stream().map(this::getCouponVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<Coupon> getQueryWrapper(CouponQueryRequest couponQueryRequest) {
        if (couponQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = couponQueryRequest.getId();
        String couponName = couponQueryRequest.getCouponName();
        Integer couponType = couponQueryRequest.getCouponType();
        String platform = couponQueryRequest.getPlatform();
        Integer status = couponQueryRequest.getStatus();
        Date startTimeBegin = couponQueryRequest.getStartTimeBegin();
        Date startTimeEnd = couponQueryRequest.getStartTimeEnd();
        Date endTimeBegin = couponQueryRequest.getEndTimeBegin();
        Date endTimeEnd = couponQueryRequest.getEndTimeEnd();
        String sortField = couponQueryRequest.getSortField();
        String sortOrder = couponQueryRequest.getSortOrder();

        QueryWrapper<Coupon> queryWrapper = new QueryWrapper<>();

        // 精确匹配条件
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotNull(couponType), "couponType", couponType);
        queryWrapper.eq(StrUtil.isNotBlank(platform), "platform", platform);
        queryWrapper.eq(ObjUtil.isNotNull(status), "status", status);

        // 模糊匹配条件
        queryWrapper.like(StrUtil.isNotBlank(couponName), "couponName", couponName);

        // 时间范围查询
        queryWrapper.ge(ObjUtil.isNotNull(startTimeBegin), "startTime", startTimeBegin);
        queryWrapper.le(ObjUtil.isNotNull(startTimeEnd), "startTime", startTimeEnd);
        queryWrapper.ge(ObjUtil.isNotNull(endTimeBegin), "endTime", endTimeBegin);
        queryWrapper.le(ObjUtil.isNotNull(endTimeEnd), "endTime", endTimeEnd);

        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);

        return queryWrapper;
    }
}