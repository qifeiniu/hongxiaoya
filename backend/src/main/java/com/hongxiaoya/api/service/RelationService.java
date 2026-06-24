package com.hongxiaoya.api.service;

import com.hongxiaoya.api.dto.VisitorRecordDto;
import com.hongxiaoya.api.dto.WhoLikesMeDto;
import com.hongxiaoya.api.entity.UserProfile;

import java.util.List;

public interface RelationService {
    void like(Long userId, Long targetId);
    void dislike(Long userId, Long targetId);
    void block(Long userId, Long targetId);
    void unblock(Long userId, Long targetId);
    List<UserProfile> getBlockList(Long userId);
    List<WhoLikesMeDto> getWhoLikesMe(Long userId);
    void unlockWhoLikesMe(Long userId, Long targetId);
    String unlockWechat(Long userId, Long targetId);
    
    void recordVisitor(Long userId, Long visitorId);
    List<VisitorRecordDto> getVisitors(Long userId);
    void unlockVisitor(Long userId, Long visitorId);
}
