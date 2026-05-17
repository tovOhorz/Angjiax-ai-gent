create table if not exists `order`
(
    id             bigint auto_increment comment 'id' primary key,
    orderId        varchar(32)                           not null comment '订单号（对外展示用）',
    userId         bigint                                not null comment '用户ID（关联user.id）',
    totalAmount    decimal(10,2)                         not null comment '订单总金额',
    discountAmount decimal(10,2) default 0               not null comment '优惠金额',
    actualPaid     decimal(10,2)                         not null comment '实付金额',
    couponId       bigint                                null comment '使用的优惠券ID（关联coupon.id）',
    orderStatus    tinyint      default 1                not null comment '订单状态：1待付款/2待发货/3待收货/4已完成/5已取消',
    paidTime       datetime                              null comment '付款时间',
    address        varchar(500)                          null comment '收货地址',
    createTime     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint      default 0                not null comment '是否删除',
    unique key uk_orderId (orderId),
    index idx_userId (userId),
    index idx_orderStatus (orderStatus),
    index idx_createTime (createTime)
    ) comment '订单' collate = utf8mb4_unicode_ci;