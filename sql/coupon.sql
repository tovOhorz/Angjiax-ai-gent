create table if not exists coupon
(
    id                 bigint auto_increment comment 'id' primary key,
    couponName         varchar(200)                          not null comment '优惠券名称',
    couponType         tinyint                               not null comment '优惠券类型：1店铺券/2平台券/3会员券',
    conditionAmount    decimal(10,2)                         null comment '满减条件（满X元可用）',
    discountAmount     decimal(10,2)                         null comment '优惠金额（减Y元）',
    discountRate       decimal(3,2)                          null comment '折扣率（如0.85表示85折）',
    startTime          datetime                              not null comment '开始时间',
    endTime            datetime                              not null comment '结束时间',
    applicableProducts text                                  null comment '适用商品ID列表（JSON数组，存商品的id）',
    platform           varchar(20)                           null comment '所属平台',
    totalQuantity      int                                   null comment '总发行量',
    usedQuantity       int          default 0                not null comment '已使用量',
    status             tinyint      default 1                not null comment '状态：1有效/0失效',
    createTime         datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime         datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete           tinyint      default 0                not null comment '是否删除',
    index idx_couponType (couponType),
    index idx_validTime (startTime, endTime),
    index idx_status (status)
    ) comment '优惠券' collate = utf8mb4_unicode_ci;