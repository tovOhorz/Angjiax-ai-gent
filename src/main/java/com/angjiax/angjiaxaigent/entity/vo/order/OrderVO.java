package com.angjiax.angjiaxaigent.entity.vo.order;

import com.angjiax.angjiaxaigent.entity.OrderItem;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderVO implements Serializable {

    private Long id;
    private String orderId;
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal actualPaid;
    private Long couponId;
    private Integer orderStatus;
    private String orderStatusText;
    private Date paidTime;
    private String address;
    private Date createTime;

    /**
     * 订单明细列表
     */
    private List<OrderItem> orderItems;

    private static final long serialVersionUID = 1L;
}