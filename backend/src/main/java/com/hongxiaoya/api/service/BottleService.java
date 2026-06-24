package com.hongxiaoya.api.service;

import java.util.List;
import java.util.Map;

public interface BottleService {
    void throwBottle(Long userId, String content);
    Map<String, Object> pickBottle(Long userId);
    List<Map<String, Object>> getRecords(Long userId, Integer type);
}
