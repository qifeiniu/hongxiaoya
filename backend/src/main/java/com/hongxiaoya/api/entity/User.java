package com.hongxiaoya.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String phone;
    private String wxUnionid;
    private String wxOpenid;
    private Integer status; // 1-正常, 2-封禁, 3-注销
    private Integer role;   // 1-普通用户, 2-红娘
    private Long referrerId; // 推荐人ID
    private Date createdAt;
    private Date updatedAt;
}
