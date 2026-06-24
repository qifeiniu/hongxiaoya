package com.hongxiaoya.api.service;

import com.hongxiaoya.api.dto.UpdateProfileReq;
import com.hongxiaoya.api.entity.UserProfile;

public interface ProfileService {
    UserProfile getProfile(Long userId);
    void updateProfile(Long userId, UpdateProfileReq req);
    void realNameAuth(Long userId, String frontImageUrl, String backImageUrl, String faceImageUrl);
    UserProfile getUserDetail(Long userId, Long targetUserId);
    void applyMatchmaker(Long userId);
}
