package com.hongxiaoya.api.dto;

import com.hongxiaoya.api.entity.UserProfile;
import lombok.Data;

import java.util.Date;

@Data
public class ConversationDto {
    private Long id;
    private Long targetId;
    private UserProfile targetProfile;
    private String lastMsgContent;
    private Date lastMsgTime;
    private Integer unreadCount;
    private Integer isTop;
}
