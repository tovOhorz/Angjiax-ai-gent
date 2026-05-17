create table if not exists cart
(
    id          bigint auto_increment comment 'id' primary key,
    userId      bigint                                not null comment '用户ID（关联user.id）',
    productId   bigint                                not null comment '商品ID（关联product.id）',
    quantity    int          default 1                not null comment '数量',
    selected    tinyint      default 1                not null comment '是否选中：1是/0否',
    createTime  datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_userId (userId),
    index idx_productId (productId),
    unique key uk_user_product (userId, productId)
    ) comment '购物车' collate = utf8mb4_unicode_ci;