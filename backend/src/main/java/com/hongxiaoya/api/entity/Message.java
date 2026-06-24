package com.hongxiaoya.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("messages")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long senderId;
    private Long receiverId;
    private Integer msgType; // 1-文字, 2-图片, 3-语音, 4-视频, 5-礼物, 6-系统
    private String content;
    private Integer isRead;
    private Integer isRecalled;
    private Date createdAt;
}
