package com.angjiax.angjiaxaigent.service;

import com.angjiax.angjiaxaigent.entity.Product;
import com.angjiax.angjiaxaigent.entity.dto.product.ProductQueryRequest;
import com.angjiax.angjiaxaigent.entity.vo.product.ProductVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author t'h
* @description 针对表【product(商品)】的数据库操作Service
* @createDate 2026-05-01 20:38:35
*/
public interface ProductService extends IService<Product> {


    /**
     * 获取商品VO
     *
     * @param product
     * @return
     */
    ProductVO getProductVO(Product product);

    /**
     * 获取商品VO列表
     *
     * @param productList
     * @return
     */
    List<ProductVO> getProductVOList(List<Product> productList);

    /**
     * 获取查询条件
     *
     * @param productQueryRequest
     * @return
     */
    QueryWrapper<Product> getQueryWrapper(ProductQueryRequest productQueryRequest);

}
