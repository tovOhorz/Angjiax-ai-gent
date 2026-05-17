package com.angjiax.angjiaxaigent.entity.dto.product;

import com.angjiax.angjiaxaigent.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProductQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 平台：淘宝/京东/拼多多
     */
    private String platform;

    /**
     * 最小原价
     */
    private BigDecimal minOriginalPrice;

    /**
     * 最大原价
     */
    private BigDecimal maxOriginalPrice;

    /**
     * 最小当前售价
     */
    private BigDecimal minCurrentPrice;

    /**
     * 最大当前售价
     */
    private BigDecimal maxCurrentPrice;

    /**
     * 最小库存
     */
    private Integer minStock;

    /**
     * 最大库存
     */
    private Integer maxStock;

    /**
     * 最小月销量
     */
    private Integer minSalesVolume;

    /**
     * 最大月销量
     */
    private Integer maxSalesVolume;

    /**
     * 最小综合评分
     */
    private BigDecimal minRating;

    /**
     * 最大综合评分
     */
    private BigDecimal maxRating;

    /**
     * 状态：1上架/0下架
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}