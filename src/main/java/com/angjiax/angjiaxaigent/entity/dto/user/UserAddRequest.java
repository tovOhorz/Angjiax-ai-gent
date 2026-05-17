package com.angjiax.angjiaxaigent.entity.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色: user, admin
     */
    private String userRole;

    /**
     * 会员等级：0普通/1黄金/2铂金/3钻石
     */
    private Integer vip_level;

    /**
     * 积分
     */
    private Integer points;

    /**
     * 手机号
     */
    private String phone;

    private static final long serialVersionUID = 1L;
}
