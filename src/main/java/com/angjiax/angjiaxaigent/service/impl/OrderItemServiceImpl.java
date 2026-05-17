package com.angjiax.angjiaxaigent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.angjiax.angjiaxaigent.entity.OrderItem;
import com.angjiax.angjiaxaigent.service.OrderItemService;
import com.angjiax.angjiaxaigent.mapper.OrderItemMapper;
import org.springframework.stereotype.Service;

/**
* @author t'h
* @description 针对表【order_item(订单明细)】的数据库操作Service实现
* @createDate 2026-05-03 14:12:06
*/
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem>
    implements OrderItemService{

}




