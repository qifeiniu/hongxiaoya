package com.hongxiaoya.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("bottles")
public class Bottle {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String content;
    private Integer status; // 0-未被捞起, 1-已被捞起
    private Long pickedBy;
    private Date createdAt;
    private Date pickedAt;
}
