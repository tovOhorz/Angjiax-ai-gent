package com.angjiax.angjiaxaigent.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.angjiax.angjiaxaigent.entity.Product;
import com.angjiax.angjiaxaigent.entity.dto.product.ProductQueryRequest;
import com.angjiax.angjiaxaigent.entity.vo.product.ProductVO;
import com.angjiax.angjiaxaigent.exception.BusinessException;
import com.angjiax.angjiaxaigent.exception.ErrorCode;
import com.angjiax.angjiaxaigent.mapper.ProductMapper;
import com.angjiax.angjiaxaigent.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author t'h
 * @description 针对表【product(商品)】的数据库操作Service实现
 * @createDate 2026-05-02
 */
@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
        implements ProductService {

    @Override
    public ProductVO getProductVO(Product product) {
        if (product == null) {
            return null;
        }
        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(product, productVO);
        return productVO;
    }

    @Override
    public List<ProductVO> getProductVOList(List<Product> productList) {
        if (CollUtil.isEmpty(productList)) {
            return new ArrayList<>();
        }
        return productList.stream().map(this::getProductVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<Product> getQueryWrapper(ProductQueryRequest productQueryRequest) {
        if (productQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = productQueryRequest.getId();
        String productName = productQueryRequest.getProductName();
        String categoryName = productQueryRequest.getCategoryName();
        String brand = productQueryRequest.getBrand();
        String platform = productQueryRequest.getPlatform();
        BigDecimal minOriginalPrice = productQueryRequest.getMinOriginalPrice();
        BigDecimal maxOriginalPrice = productQueryRequest.getMaxOriginalPrice();
        BigDecimal minCurrentPrice = productQueryRequest.getMinCurrentPrice();
        BigDecimal maxCurrentPrice = productQueryRequest.getMaxCurrentPrice();
        Integer minStock = productQueryRequest.getMinStock();
        Integer maxStock = productQueryRequest.getMaxStock();
        Integer minSalesVolume = productQueryRequest.getMinSalesVolume();
        Integer maxSalesVolume = productQueryRequest.getMaxSalesVolume();
        BigDecimal minRating = productQueryRequest.getMinRating();
        BigDecimal maxRating = productQueryRequest.getMaxRating();
        Integer status = productQueryRequest.getStatus();
        String sortField = productQueryRequest.getSortField();
        String sortOrder = productQueryRequest.getSortOrder();

        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();

        // 精确匹配条件
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(platform), "platform", platform);
        queryWrapper.eq(ObjUtil.isNotNull(status), "status", status);

        // 范围查询
        queryWrapper.ge(ObjUtil.isNotNull(minOriginalPrice), "originalPrice", minOriginalPrice);
        queryWrapper.le(ObjUtil.isNotNull(maxOriginalPrice), "originalPrice", maxOriginalPrice);
        queryWrapper.ge(ObjUtil.isNotNull(minCurrentPrice), "currentPrice", minCurrentPrice);
        queryWrapper.le(ObjUtil.isNotNull(maxCurrentPrice), "currentPrice", maxCurrentPrice);
        queryWrapper.ge(ObjUtil.isNotNull(minStock), "stock", minStock);
        queryWrapper.le(ObjUtil.isNotNull(maxStock), "stock", maxStock);
        queryWrapper.ge(ObjUtil.isNotNull(minSalesVolume), "salesVolume", minSalesVolume);
        queryWrapper.le(ObjUtil.isNotNull(maxSalesVolume), "salesVolume", maxSalesVolume);
        queryWrapper.ge(ObjUtil.isNotNull(minRating), "rating", minRating);
        queryWrapper.le(ObjUtil.isNotNull(maxRating), "rating", maxRating);

        // 模糊匹配条件
        queryWrapper.like(StrUtil.isNotBlank(productName), "productName", productName);
        queryWrapper.like(StrUtil.isNotBlank(categoryName), "categoryName", categoryName);
        queryWrapper.like(StrUtil.isNotBlank(brand), "brand", brand);

        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField),
                "ascend".equals(sortOrder),
                sortField);

        return queryWrapper;
    }
}