package com.angjiax.angjiaxaigent.controller;

import com.angjiax.angjiaxaigent.anotation.AuthCheck;
import com.angjiax.angjiaxaigent.common.BaseResponse;
import com.angjiax.angjiaxaigent.common.DeleteRequest;
import com.angjiax.angjiaxaigent.common.ResultUtils;
import com.angjiax.angjiaxaigent.constant.UserConstant;
import com.angjiax.angjiaxaigent.entity.Product;
import com.angjiax.angjiaxaigent.entity.dto.product.ProductAddRequest;
import com.angjiax.angjiaxaigent.entity.dto.product.ProductQueryRequest;
import com.angjiax.angjiaxaigent.entity.dto.product.ProductUpdateRequest;
import com.angjiax.angjiaxaigent.entity.vo.product.ProductVO;
import com.angjiax.angjiaxaigent.exception.BusinessException;
import com.angjiax.angjiaxaigent.exception.ErrorCode;
import com.angjiax.angjiaxaigent.exception.ThrowUtils;
import com.angjiax.angjiaxaigent.manager.CacheManager;
import com.angjiax.angjiaxaigent.service.ProductService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Resource
    private ProductService productService;

    @Resource
    private CacheManager cacheManager;

    /**
     * 创建商品（仅管理员）
     */
    @PostMapping("/add")
    //@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addProduct(@RequestBody ProductAddRequest productAddRequest) {
        ThrowUtils.throwIf(productAddRequest == null, ErrorCode.PARAMS_ERROR);
        Product product = new Product();
        BeanUtils.copyProperties(productAddRequest, product);
        boolean result = productService.save(product);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(product.getId());
    }

    /**
     * 根据 id 获取商品（仅管理员）
     */
    @GetMapping("/get")
    //@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Product> getProductById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        String cacheKey = "product:" + id;
        // 查缓存
        Product product = (Product) cacheManager.get(cacheKey);
        if (product != null) {
            return ResultUtils.success(product);
        }
        product = productService.getById(id);
        // 如果查询不存在的商品ID，缓存空值防止缓存穿透
        if (product == null) {
            cacheManager.put(cacheKey, "NULL", TimeUnit.MINUTES.toMillis(5));
            ThrowUtils.throwIf(true, ErrorCode.NOT_FOUND_ERROR);
        } else {
            // 缓存真实数据
            long ttl = TimeUnit.HOURS.toMillis(1) + ThreadLocalRandom.current().nextInt(0, 10000);
            cacheManager.put(cacheKey, product, TimeUnit.HOURS.toMillis(ttl));
        }
        return ResultUtils.success(product);
    }

    /**
     * 根据 id 获取商品VO
     */
    @GetMapping("/get/vo")
    public BaseResponse<ProductVO> getProductVOById(long id) {
        BaseResponse<Product> response = getProductById(id);
        Product product = response.getData();
        return ResultUtils.success(productService.getProductVO(product));
    }

    /**
     * 删除商品（仅管理员）
     */
    @PostMapping("/delete")
    //@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteProduct(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = productService.removeById(deleteRequest.getId());
        // 删除缓存
        cacheManager.remove("product:" + deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新商品（仅管理员）
     */
    @PostMapping("/update")
    //@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateProduct(@RequestBody ProductUpdateRequest productUpdateRequest) {
        if (productUpdateRequest == null || productUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Product product = new Product();
        BeanUtils.copyProperties(productUpdateRequest, product);
        boolean result = productService.updateById(product);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 清除缓存
        cacheManager.remove("product:" + product.getId());
        return ResultUtils.success(true);
    }

    /**
     * 分页获取商品列表（管理员）
     */
    @PostMapping("/list/page")
    //@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Product>> listProductByPage(@RequestBody ProductQueryRequest productQueryRequest) {
        ThrowUtils.throwIf(productQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = productQueryRequest.getCurrent();
        long pageSize = productQueryRequest.getPageSize();
        Page<Product> productPage = productService.page(new Page<>(current, pageSize),
                productService.getQueryWrapper(productQueryRequest));
        return ResultUtils.success(productPage);
    }

    /**
     * 分页获取商品VO列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ProductVO>> listProductVOByPage(@RequestBody ProductQueryRequest productQueryRequest) {
        ThrowUtils.throwIf(productQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = productQueryRequest.getCurrent();
        long pageSize = productQueryRequest.getPageSize();
        Page<Product> productPage = productService.page(new Page<>(current, pageSize),
                productService.getQueryWrapper(productQueryRequest));
        Page<ProductVO> productVOPage = new Page<>(current, pageSize, productPage.getTotal());
        List<ProductVO> productVOList = productService.getProductVOList(productPage.getRecords());
        productVOPage.setRecords(productVOList);
        return ResultUtils.success(productVOPage);
    }

}
