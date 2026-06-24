package com.hongxiaoya.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("activities")
public class Activity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String coverImg;
    private String description;
    private String requirements; // 报名要求
    private Long creatorId; // 红娘ID
    private Integer maleQuota;
    private Integer femaleQuota;
    private Integer price; // 报名费(元或鸭蛋)
    private Date startTime;
    private Integer duration; // 持续时长(分钟)
    private Integer status; // 0-报名中, 1-进行中, 2-已结束
    private Date createdAt;
}
