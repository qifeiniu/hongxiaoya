package com.hongxiaoya.api.service;

import com.hongxiaoya.api.entity.Activity;

import java.util.List;

public interface ActivityService {
    List<Activity> getActivityList();
    void createActivity(Long creatorId, Activity activity);
    void joinActivity(Long userId, Long activityId);
}
