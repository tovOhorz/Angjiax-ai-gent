create table if not exists user_coupon
(
    id          bigint auto_increment comment 'id' primary key,
    userId      bigint                                not null comment '用户ID（关联user.id）',
    couponId    bigint                                not null comment '优惠券ID（关联coupon.id）',
    status      tinyint      default 1                not null comment '状态：1未使用/2已使用/3已过期',
    receivedAt  datetime     default CURRENT_TIMESTAMP not null comment '领取时间',
    usedAt      datetime                              null comment '使用时间',
    orderId     varchar(32)                           null comment '关联订单ID',
    createTime  datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_userId (userId),
    index idx_couponId (couponId),
    index idx_status (status),
    unique key uk_user_coupon (userId, couponId)
    ) comment '用户优惠券记录' collate = utf8mb4_unicode_ci;