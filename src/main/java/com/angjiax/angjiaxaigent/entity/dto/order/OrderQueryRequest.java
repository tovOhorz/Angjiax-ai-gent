package com.angjiax.angjiaxaigent.entity.dto.order;

import com.angjiax.angjiaxaigent.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrderQueryRequest extends PageRequest implements Serializable {

    private Long id;
    private String orderId;
    private Long userId;
    private Integer orderStatus;
    private BigDecimal minTotalAmount;
    private BigDecimal maxTotalAmount;
    private BigDecimal minActualPaid;
    private BigDecimal maxActualPaid;
    private Date createTimeBegin;
    private Date createTimeEnd;
    private Date paidTimeBegin;
    private Date paidTimeEnd;

    private static final long serialVersionUID = 1L;
}