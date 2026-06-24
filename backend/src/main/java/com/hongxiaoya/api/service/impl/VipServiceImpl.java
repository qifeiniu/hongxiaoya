package com.hongxiaoya.api.service.impl;

import cn.hutool.core.date.DateUtil;
import com.hongxiaoya.api.entity.VipMembership;
import com.hongxiaoya.api.mapper.VipMembershipMapper;
import com.hongxiaoya.api.service.VipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class VipServiceImpl implements VipService {

    private final VipMembershipMapper vipMembershipMapper;

    @Override
    public VipMembership getVipInfo(Long userId) {
        return vipMembershipMapper.selectById(userId);
    }

    @Override
    public void buyVip(Long userId, Integer level, Integer months) {
        VipMembership vip = vipMembershipMapper.selectById(userId);
        Date now = new Date();
        if (vip == null) {
            vip = new VipMembership();
            vip.setUserId(userId);
            vip.setLevel(level);
            vip.setExpireAt(DateUtil.offsetMonth(now, months));
            vip.setCreatedAt(now);
            vip.setUpdatedAt(now);
            vipMembershipMapper.insert(vip);
        } else {
            Date expireAt = vip.getExpireAt();
            if (expireAt == null || expireAt.before(now)) {
                expireAt = now;
            }
            vip.setExpireAt(DateUtil.offsetMonth(expireAt, months));
            vip.setUpdatedAt(now);
            vipMembershipMapper.updateById(vip);
        }
    }
}
