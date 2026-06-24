package com.hongxiaoya.api.service;

import com.hongxiaoya.api.entity.VipMembership;

public interface VipService {
    VipMembership getVipInfo(Long userId);
    void buyVip(Long userId, Integer level, Integer months);
}
