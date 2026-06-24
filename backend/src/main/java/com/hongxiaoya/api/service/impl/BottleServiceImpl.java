package com.hongxiaoya.api.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongxiaoya.api.entity.Bottle;
import com.hongxiaoya.api.entity.UserProfile;
import com.hongxiaoya.api.entity.VipMembership;
import com.hongxiaoya.api.mapper.BottleMapper;
import com.hongxiaoya.api.mapper.UserProfileMapper;
import com.hongxiaoya.api.service.BottleService;
import com.hongxiaoya.api.service.VipService;
import com.hongxiaoya.api.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BottleServiceImpl implements BottleService {

    private final BottleMapper bottleMapper;
    private final UserProfileMapper userProfileMapper;
    private final VipService vipService;
    private final WalletService walletService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void throwBottle(Long userId, String content) {
        checkAndDeductBottleQuota(userId, "throw");

        Bottle bottle = new Bottle();
        bottle.setUserId(userId);
        bottle.setContent(content);
        bottle.setStatus(0);
        bottle.setCreatedAt(new Date());
        bottleMapper.insert(bottle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> pickBottle(Long userId) {
        checkAndDeductBottleQuota(userId, "pick");

        // 获取当前用户性别
        UserProfile currentUser = userProfileMapper.selectById(userId);
        Integer gender = currentUser != null ? currentUser.getGender() : null;

        // 仅可被异性捡到
        QueryWrapper<Bottle> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0).ne("user_id", userId);
        if (gender != null) {
            Integer targetGender = gender == 1 ? 2 : 1;
            queryWrapper.inSql("user_id", "SELECT user_id FROM user_profiles WHERE gender = " + targetGender);
        }
        
        queryWrapper.orderByAsc("RAND()").last("LIMIT 1");
        Bottle bottle = bottleMapper.selectOne(queryWrapper);

        if (bottle == null) {
            return null;
        }

        bottle.setStatus(1);
        bottle.setPickedBy(userId);
        bottle.setPickedAt(new Date());
        bottleMapper.updateById(bottle);

        UserProfile profile = userProfileMapper.selectById(bottle.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("id", bottle.getId());
        result.put("userId", bottle.getUserId());
        result.put("content", bottle.getContent());
        result.put("nickname", profile != null ? profile.getNickname() : "神秘人");
        result.put("avatar", profile != null ? profile.getAvatar() : "");
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getRecords(Long userId, Integer type) {
        LambdaQueryWrapper<Bottle> queryWrapper = new LambdaQueryWrapper<>();
        if (type == 1) {
            // 我扔的
            queryWrapper.eq(Bottle::getUserId, userId).orderByDesc(Bottle::getCreatedAt);
        } else if (type == 2) {
            // 我捞的
            queryWrapper.eq(Bottle::getPickedBy, userId).orderByDesc(Bottle::getPickedAt);
        } else {
            return java.util.Collections.emptyList();
        }

        List<Bottle> bottles = bottleMapper.selectList(queryWrapper);

        return bottles.stream().map(bottle -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", bottle.getId());
            map.put("content", bottle.getContent());
            map.put("status", bottle.getStatus());
            map.put("createdAt", bottle.getCreatedAt());
            map.put("pickedAt", bottle.getPickedAt());
            
            // 如果是我捞的，展示扔瓶子人的信息；如果是我扔的且被捞了，展示捞起人的信息
            Long targetUserId = null;
            if (type == 1 && bottle.getStatus() == 1) {
                targetUserId = bottle.getPickedBy();
            } else if (type == 2) {
                targetUserId = bottle.getUserId();
            }

            if (targetUserId != null) {
                UserProfile profile = userProfileMapper.selectById(targetUserId);
                if (profile != null) {
                    map.put("targetUserId", profile.getUserId());
                    map.put("targetNickname", profile.getNickname());
                    map.put("targetAvatar", profile.getAvatar());
                }
            }
            return map;
        }).collect(Collectors.toList());
    }

    private void checkAndDeductBottleQuota(Long userId, String action) {
        VipMembership vipInfo = vipService.getVipInfo(userId);
        boolean isVip = (vipInfo != null && vipInfo.getExpireAt().after(new Date()));
        int maxFreeCount = isVip ? 20 : 5;

        Date todayStart = DateUtil.beginOfDay(new Date());
        Date todayEnd = DateUtil.endOfDay(new Date());

        LambdaQueryWrapper<Bottle> wrapper = new LambdaQueryWrapper<>();
        if ("throw".equals(action)) {
            wrapper.eq(Bottle::getUserId, userId);
        } else {
            wrapper.eq(Bottle::getPickedBy, userId);
        }
        wrapper.between(Bottle::getCreatedAt, todayStart, todayEnd);

        Long todayCount = bottleMapper.selectCount(wrapper);
        if (todayCount >= maxFreeCount) {
            // 超过免费次数，扣除10个鸭蛋
            boolean success = walletService.deductBalance(userId, 10, "bottle_extra_" + action, null, null);
            if (!success) {
                throw new RuntimeException("今日免费次数已用完，鸭蛋余额不足（需要10个鸭蛋）");
            }
        }
    }
}
