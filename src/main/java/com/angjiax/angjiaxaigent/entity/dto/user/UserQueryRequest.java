package com.angjiax.angjiaxaigent.entity.dto.user;

import com.angjiax.angjiaxaigent.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
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
