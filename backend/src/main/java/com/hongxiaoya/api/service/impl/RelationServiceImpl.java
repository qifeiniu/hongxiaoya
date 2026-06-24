package com.hongxiaoya.api.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hongxiaoya.api.dto.VisitorRecordDto;
import com.hongxiaoya.api.dto.WhoLikesMeDto;
import com.hongxiaoya.api.entity.UnlockRecord;
import com.hongxiaoya.api.entity.UserProfile;
import com.hongxiaoya.api.entity.UserRelation;
import com.hongxiaoya.api.entity.VipMembership;
import com.hongxiaoya.api.entity.VisitorRecord;
import com.hongxiaoya.api.mapper.UnlockRecordMapper;
import com.hongxiaoya.api.mapper.UserProfileMapper;
import com.hongxiaoya.api.mapper.UserRelationMapper;
import com.hongxiaoya.api.mapper.VisitorRecordMapper;
import com.hongxiaoya.api.service.RelationService;
import com.hongxiaoya.api.service.VipService;
import com.hongxiaoya.api.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelationServiceImpl implements RelationService {

    private final UserRelationMapper userRelationMapper;
    private final UserProfileMapper userProfileMapper;
    private final UnlockRecordMapper unlockRecordMapper;
    private final VisitorRecordMapper visitorRecordMapper;
    private final WalletService walletService;
    private final VipService vipService;

    @Override
    public void like(Long userId, Long targetId) {
        UserRelation relation = new UserRelation();
        relation.setUserId(userId);
        relation.setTargetId(targetId);
        relation.setRelationType(1); // 1-喜欢
        relation.setCreatedAt(new Date());
        try {
            userRelationMapper.insert(relation);
        } catch (Exception e) {
            // Ignore duplicate key exception
        }
    }

    @Override
    public void dislike(Long userId, Long targetId) {
        UserRelation relation = new UserRelation();
        relation.setUserId(userId);
        relation.setTargetId(targetId);
        relation.setRelationType(2); // 2-不喜欢
        relation.setCreatedAt(new Date());
        try {
            userRelationMapper.insert(relation);
        } catch (Exception e) {
            // Ignore duplicate key exception
        }
    }

    @Override
    public void block(Long userId, Long targetId) {
        UserRelation relation = new UserRelation();
        relation.setUserId(userId);
        relation.setTargetId(targetId);
        relation.setRelationType(3); // 3-黑名单
        relation.setCreatedAt(new Date());
        try {
            userRelationMapper.insert(relation);
        } catch (Exception e) {
            // If already exists, maybe update it
            UserRelation exist = userRelationMapper.selectOne(new LambdaQueryWrapper<UserRelation>()
                    .eq(UserRelation::getUserId, userId)
                    .eq(UserRelation::getTargetId, targetId));
            if (exist != null) {
                exist.setRelationType(3);
                userRelationMapper.updateById(exist);
            }
        }
    }

    @Override
    public void unblock(Long userId, Long targetId) {
        userRelationMapper.delete(new LambdaQueryWrapper<UserRelation>()
                .eq(UserRelation::getUserId, userId)
                .eq(UserRelation::getTargetId, targetId)
                .eq(UserRelation::getRelationType, 3));
    }

    @Override
    public List<UserProfile> getBlockList(Long userId) {
        List<UserRelation> relations = userRelationMapper.selectList(new LambdaQueryWrapper<UserRelation>()
                .eq(UserRelation::getUserId, userId)
                .eq(UserRelation::getRelationType, 3)
                .orderByDesc(UserRelation::getCreatedAt));

        if (relations.isEmpty()) return new ArrayList<>();

        List<Long> targetIds = relations.stream().map(UserRelation::getTargetId).collect(Collectors.toList());
        return userProfileMapper.selectBatchIds(targetIds);
    }

    @Override
    public List<WhoLikesMeDto> getWhoLikesMe(Long userId) {
        List<UserRelation> relations = userRelationMapper.selectList(new LambdaQueryWrapper<UserRelation>()
                .eq(UserRelation::getTargetId, userId)
                .eq(UserRelation::getRelationType, 1)
                .orderByDesc(UserRelation::getCreatedAt));

        if (relations.isEmpty()) return new ArrayList<>();

        List<Long> senderIds = relations.stream().map(UserRelation::getUserId).collect(Collectors.toList());
        Map<Long, UserProfile> profileMap = userProfileMapper.selectBatchIds(senderIds).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, p -> p));

        VipMembership vipInfo = vipService.getVipInfo(userId);
        boolean isVip = vipInfo != null && vipInfo.getExpireAt() != null && vipInfo.getExpireAt().after(new Date());

        List<UnlockRecord> unlockRecords = unlockRecordMapper.selectList(new LambdaQueryWrapper<UnlockRecord>()
                .eq(UnlockRecord::getUserId, userId)
                .eq(UnlockRecord::getUnlockType, 3));
        List<Long> unlockedIds = unlockRecords.stream().map(UnlockRecord::getTargetId).collect(Collectors.toList());

        List<WhoLikesMeDto> dtos = new ArrayList<>();
        for (UserRelation rel : relations) {
            WhoLikesMeDto dto = new WhoLikesMeDto();
            dto.setId(rel.getId());
            dto.setCreatedAt(rel.getCreatedAt());
            
            UserProfile profile = profileMap.get(rel.getUserId());
            if (profile != null) {
                boolean isUnlocked = isVip || unlockedIds.contains(rel.getUserId());
                dto.setIsUnlocked(isUnlocked);
                
                if (isUnlocked) {
                    dto.setUserProfile(profile);
                } else {
                    UserProfile blurred = new UserProfile();
                    blurred.setUserId(profile.getUserId());
                    blurred.setNickname("某位心动嘉宾");
                    blurred.setAvatar("/static/avatars/default-avatar.png");
                    dto.setUserProfile(blurred);
                }
            }
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    @Transactional
    public void unlockWhoLikesMe(Long userId, Long targetId) {
        VipMembership vipInfo = vipService.getVipInfo(userId);
        if (vipInfo != null && vipInfo.getExpireAt() != null && vipInfo.getExpireAt().after(new Date())) {
            return;
        }

        Long count = unlockRecordMapper.selectCount(new LambdaQueryWrapper<UnlockRecord>()
                .eq(UnlockRecord::getUserId, userId)
                .eq(UnlockRecord::getTargetId, targetId)
                .eq(UnlockRecord::getUnlockType, 3));
        
        if (count > 0) return;

        int cost = 50;
        boolean deductSuccess = walletService.deductBalance(userId, cost, "unlock_who_likes_me", String.valueOf(targetId), targetId);
        if (!deductSuccess) {
            throw new RuntimeException("鸭蛋余额不足");
        }

        UnlockRecord newRecord = new UnlockRecord();
        newRecord.setUserId(userId);
        newRecord.setTargetId(targetId);
        newRecord.setUnlockType(3); // 3-谁喜欢我
        newRecord.setCostEggs(cost);
        newRecord.setCreatedAt(new Date());
        unlockRecordMapper.insert(newRecord);
    }

    @Override
    @Transactional
    public String unlockWechat(Long userId, Long targetId) {
        // Check if already unlocked
        QueryWrapper<UnlockRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("target_id", targetId).eq("unlock_type", 1);
        UnlockRecord record = unlockRecordMapper.selectOne(queryWrapper);
        
        UserProfile targetProfile = userProfileMapper.selectById(targetId);
        if (targetProfile == null || targetProfile.getWechatId() == null) {
            throw new RuntimeException("对方未填写微信号");
        }

        if (record != null) {
            return targetProfile.getWechatId();
        }

        VipMembership vipInfo = vipService.getVipInfo(userId);
        boolean isVip = vipInfo != null && vipInfo.getExpireAt().after(new Date());

        int cost = isVip ? 50 : 100;
        boolean deductSuccess = walletService.deductBalance(userId, cost, "unlock_wechat", String.valueOf(targetId), targetId);
        if (!deductSuccess) {
            throw new RuntimeException("鸭蛋余额不足");
        }

        UnlockRecord newRecord = new UnlockRecord();
        newRecord.setUserId(userId);
        newRecord.setTargetId(targetId);
        newRecord.setUnlockType(1);
        newRecord.setCostEggs(cost);
        newRecord.setCreatedAt(new Date());
        unlockRecordMapper.insert(newRecord);

        return targetProfile.getWechatId();
    }

    @Override
    public void recordVisitor(Long userId, Long visitorId) {
        if (userId.equals(visitorId)) return;
        
        // Check if already visited recently (e.g. within 1 hour), to avoid spamming
        Date oneHourAgo = DateUtil.offsetHour(new Date(), -1);
        Long count = visitorRecordMapper.selectCount(new LambdaQueryWrapper<VisitorRecord>()
                .eq(VisitorRecord::getUserId, userId)
                .eq(VisitorRecord::getVisitorId, visitorId)
                .ge(VisitorRecord::getVisitTime, oneHourAgo));
        
        if (count == 0) {
            VisitorRecord record = new VisitorRecord();
            record.setUserId(userId);
            record.setVisitorId(visitorId);
            record.setVisitTime(new Date());
            visitorRecordMapper.insert(record);
        }
    }

    @Override
    public List<VisitorRecordDto> getVisitors(Long userId) {
        // 最近3个月
        Date threeMonthsAgo = DateUtil.offsetMonth(new Date(), -3);
        List<VisitorRecord> records = visitorRecordMapper.selectList(new LambdaQueryWrapper<VisitorRecord>()
                .eq(VisitorRecord::getUserId, userId)
                .ge(VisitorRecord::getVisitTime, threeMonthsAgo)
                .orderByDesc(VisitorRecord::getVisitTime));

        if (records.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> visitorIds = records.stream().map(VisitorRecord::getVisitorId).distinct().collect(Collectors.toList());
        List<UserProfile> profiles = userProfileMapper.selectBatchIds(visitorIds);
        Map<Long, UserProfile> profileMap = profiles.stream().collect(Collectors.toMap(UserProfile::getUserId, p -> p));

        VipMembership vipInfo = vipService.getVipInfo(userId);
        boolean isVip = vipInfo != null && vipInfo.getExpireAt().after(new Date());

        // Get unlocked records
        List<UnlockRecord> unlockRecords = unlockRecordMapper.selectList(new LambdaQueryWrapper<UnlockRecord>()
                .eq(UnlockRecord::getUserId, userId)
                .eq(UnlockRecord::getUnlockType, 2));
        List<Long> unlockedIds = unlockRecords.stream().map(UnlockRecord::getTargetId).collect(Collectors.toList());

        List<VisitorRecordDto> dtos = new ArrayList<>();
        for (VisitorRecord record : records) {
            VisitorRecordDto dto = new VisitorRecordDto();
            dto.setId(record.getId());
            dto.setVisitTime(record.getVisitTime());
            
            UserProfile profile = profileMap.get(record.getVisitorId());
            if (profile != null) {
                boolean isUnlocked = isVip || unlockedIds.contains(record.getVisitorId());
                dto.setIsUnlocked(isUnlocked);
                
                if (isUnlocked) {
                    dto.setVisitorProfile(profile);
                } else {
                    // Blur the profile
                    UserProfile blurred = new UserProfile();
                    blurred.setUserId(profile.getUserId());
                    blurred.setNickname("神秘访客");
                    blurred.setAvatar("/static/avatars/default-avatar.png");
                    dto.setVisitorProfile(blurred);
                }
            }
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    @Transactional
    public void unlockVisitor(Long userId, Long visitorId) {
        VipMembership vipInfo = vipService.getVipInfo(userId);
        if (vipInfo != null && vipInfo.getExpireAt().after(new Date())) {
            return; // VIP doesn't need to unlock
        }

        Long count = unlockRecordMapper.selectCount(new LambdaQueryWrapper<UnlockRecord>()
                .eq(UnlockRecord::getUserId, userId)
                .eq(UnlockRecord::getTargetId, visitorId)
                .eq(UnlockRecord::getUnlockType, 2));
        
        if (count > 0) return; // already unlocked

        int cost = 50;
        boolean deductSuccess = walletService.deductBalance(userId, cost, "unlock_visitor", String.valueOf(visitorId), visitorId);
        if (!deductSuccess) {
            throw new RuntimeException("鸭蛋余额不足");
        }

        UnlockRecord newRecord = new UnlockRecord();
        newRecord.setUserId(userId);
        newRecord.setTargetId(visitorId);
        newRecord.setUnlockType(2); // 2-访客记录
        newRecord.setCostEggs(cost);
        newRecord.setCreatedAt(new Date());
        unlockRecordMapper.insert(newRecord);
    }
}
