package com.hongxiaoya.api.service;

import com.hongxiaoya.api.entity.UserProfile;
import java.util.List;

public interface HomeService {
    List<UserProfile> getRecommendList(Long userId);
    void sendRose(Long userId, Long targetUserId);
    void like(Long userId, Long targetUserId);
    void dislike(Long userId, Long targetUserId);
    String unlockWechat(Long userId, Long targetUserId);
}
