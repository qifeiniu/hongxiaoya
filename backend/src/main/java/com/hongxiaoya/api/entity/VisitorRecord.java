package com.hongxiaoya.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("visitor_records")
public class VisitorRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId; // The user whose profile is visited
    private Long visitorId; // The user who visits the profile
    private Date visitTime;
}
