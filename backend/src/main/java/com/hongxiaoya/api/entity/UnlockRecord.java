package com.hongxiaoya.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("unlock_records")
public class UnlockRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long targetId;
    private Integer unlockType; // 1-微信号, 2-访客记录, 3-谁喜欢我
    private Integer costEggs;
    private Date createdAt;
}
