package com.hongxiaoya.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hongxiaoya.api.entity.UserProfile;
import com.hongxiaoya.api.entity.UserRelation;
import com.hongxiaoya.api.entity.VipMembership;
import com.hongxiaoya.api.mapper.UserProfileMapper;
import com.hongxiaoya.api.mapper.UserRelationMapper;
import com.hongxiaoya.api.service.HomeService;
import com.hongxiaoya.api.service.MessageService;
import com.hongxiaoya.api.service.VipService;
import com.hongxiaoya.api.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final UserProfileMapper userProfileMapper;
    private final UserRelationMapper userRelationMapper;
    private final WalletService walletService;
    private final VipService vipService;
    private final MessageService messageService;

    @Override
    public List<UserProfile> getRecommendList(Long userId) {
        // 获取当前用户性别
        UserProfile currentUser = userProfileMapper.selectById(userId);
        Integer gender = currentUser != null ? currentUser.getGender() : null;

        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<UserProfile>()
                .ne(UserProfile::getUserId, userId);
        
        // 推荐机制与展示：首页推荐仅展示异性
        if (gender != null) {
            // 如果我是男(1)，看女(2)；如果我是女(2)，看男(1)
            Integer targetGender = gender == 1 ? 2 : 1;
            wrapper.eq(UserProfile::getGender, targetGender);
        }

        // 排除掉已经屏蔽的用户
        List<UserRelation> blockRelations = userRelationMapper.selectList(new LambdaQueryWrapper<UserRelation>()
                .eq(UserRelation::getUserId, userId)
                .eq(UserRelation::getRelationType, 3));
        if (!blockRelations.isEmpty()) {
            List<Long> blockedIds = blockRelations.stream()
                    .map(UserRelation::getTargetId)
                    .collect(java.util.stream.Collectors.toList());
            wrapper.notIn(UserProfile::getUserId, blockedIds);
        }

        // 首页推荐的人改为都是完成三重认证的人员（实名认证+学历认证）
        wrapper.eq(UserProfile::getIsRealAuth, 1)
               .eq(UserProfile::getIsEduAuth, 1);

        // MVP: 查出除了自己以外的其他异性用户的 Profile
        return userProfileMapper.selectList(wrapper.last("LIMIT 20"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendRose(Long userId, Long targetUserId) {
        // 扣除 20 鸭蛋
        boolean success = walletService.deductBalance(userId, 20, "send_rose", null, targetUserId);
        if (!success) {
            throw new RuntimeException("鸭蛋余额不足");
        }
        
        // 写入一条打招呼/送花消息记录，并创建私聊会话
        messageService.sendMessage(userId, targetUserId, 5, "赠送了玫瑰花 🌹");
        
        System.out.println("用户 " + userId + " 向 " + targetUserId + " 赠送了一朵玫瑰，开启了私聊");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(Long userId, Long targetUserId) {
        saveRelation(userId, targetUserId, 1);
        
        // 检查对方是否也喜欢我
        UserRelation reverseRelation = userRelationMapper.selectOne(new LambdaQueryWrapper<UserRelation>()
                .eq(UserRelation::getUserId, targetUserId)
                .eq(UserRelation::getTargetId, userId)
                .eq(UserRelation::getRelationType, 1));
        
        if (reverseRelation != null) {
            // 双方互相喜欢，自动匹配开启聊天模式
            System.out.println("用户 " + userId + " 和 " + targetUserId + " 互相喜欢，开启私聊");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dislike(Long userId, Long targetUserId) {
        saveRelation(userId, targetUserId, 2);
    }

    private void saveRelation(Long userId, Long targetId, Integer type) {
        UserRelation relation = userRelationMapper.selectOne(new LambdaQueryWrapper<UserRelation>()
                .eq(UserRelation::getUserId, userId)
                .eq(UserRelation::getTargetId, targetId));
        
        if (relation == null) {
            relation = new UserRelation();
            relation.setUserId(userId);
            relation.setTargetId(targetId);
            relation.setRelationType(type);
            userRelationMapper.insert(relation);
        } else {
            relation.setRelationType(type);
            relation.setCreatedAt(new Date());
            userRelationMapper.updateById(relation);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String unlockWechat(Long userId, Long targetUserId) {
        VipMembership vipInfo = vipService.getVipInfo(userId);
        boolean isVip = (vipInfo != null && vipInfo.getExpireAt() != null && vipInfo.getExpireAt().after(new Date()));
        
        int cost = isVip ? 50 : 100;
        boolean success = walletService.deductBalance(userId, cost, "unlock_wechat", null, targetUserId);
        if (!success) {
            throw new RuntimeException("鸭蛋余额不足，需要" + cost + "个鸭蛋");
        }

        UserProfile targetProfile = userProfileMapper.selectById(targetUserId);
        if (targetProfile == null || targetProfile.getWechatId() == null) {
            throw new RuntimeException("对方未填写微信号");
        }
        
        return targetProfile.getWechatId();
    }
}
