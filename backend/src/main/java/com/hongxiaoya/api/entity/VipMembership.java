package com.hongxiaoya.api.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("vip_memberships")
public class VipMembership {
    @TableId
    private Long userId;
    private Integer level;
    private Date expireAt;
    private Date createdAt;
    private Date updatedAt;
}
