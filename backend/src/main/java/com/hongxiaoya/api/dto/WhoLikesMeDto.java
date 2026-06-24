package com.hongxiaoya.api.dto;

import com.hongxiaoya.api.entity.UserProfile;
import lombok.Data;
import java.util.Date;

@Data
public class WhoLikesMeDto {
    private Long id;
    private Date createdAt;
    private UserProfile userProfile; // blurred if not unlocked
    private Boolean isUnlocked;
}
