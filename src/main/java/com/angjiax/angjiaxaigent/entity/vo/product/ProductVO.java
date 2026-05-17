package com.angjiax.angjiaxaigent.entity.vo.product;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ProductVO implements Serializable {

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
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 当前售价
     */
    private BigDecimal currentPrice;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 月销量
     */
    private Integer salesVolume;

    /**
     * 综合评分
     */
    private BigDecimal rating;

    /**
     * 平台：淘宝/京东/拼多多
     */
    private String platform;

    /**
     * 商品链接
     */
    private String platformUrl;

    /**
     * 商品图片
     */
    private String imageUrl;

    /**
     * 商品描述
     */
    private String productDesc;

    /**
     * 状态：1上架/0下架
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}