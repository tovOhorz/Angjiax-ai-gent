package com.angjiax.angjiaxaigent.entity.vo.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 用户
 * @TableName user
 */
@Data
public class LoginUserVO {
    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
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

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    private static final  long serialVersionUID = 1l;
}