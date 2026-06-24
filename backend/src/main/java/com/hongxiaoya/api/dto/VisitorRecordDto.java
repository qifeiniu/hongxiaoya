package com.hongxiaoya.api.dto;

import com.hongxiaoya.api.entity.UserProfile;
import lombok.Data;

import java.util.Date;

@Data
public class VisitorRecordDto {
    private Long id;
    private Date visitTime;
    private UserProfile visitorProfile; // blurred if not unlocked
    private Boolean isUnlocked;
}
