package com.hongxiaoya.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongxiaoya.api.entity.Activity;
import com.hongxiaoya.api.entity.User;
import com.hongxiaoya.api.mapper.ActivityMapper;
import com.hongxiaoya.api.mapper.UserMapper;
import com.hongxiaoya.api.service.ActivityService;
import com.hongxiaoya.api.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;
    private final WalletService walletService;

    @Override
    public List<Activity> getActivityList() {
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_at");
        return activityMapper.selectList(queryWrapper);
    }

    @Override
    public void createActivity(Long creatorId, Activity activity) {
        User user = userMapper.selectById(creatorId);
        if (user == null || user.getRole() != 2) {
            throw new RuntimeException("只有红娘才可以创建相亲活动");
        }
        if (activity.getPrice() == null || activity.getPrice() < 10) {
            throw new RuntimeException("活动报名费不得低于10元");
        }

        activity.setCreatorId(creatorId);
        activity.setStatus(0);
        activity.setCreatedAt(new Date());
        activityMapper.insert(activity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinActivity(Long userId, Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        if (activity.getStatus() != 0) {
            throw new RuntimeException("活动不在报名中状态");
        }

        // 模拟支付报名费，由于 price 单位是元，此处简化为扣除等额的鸭蛋或直接扣款
        // 这里为了流程跑通，假设 price 是需要支付的鸭蛋数量，实际需走微信/支付宝支付
        boolean success = walletService.deductBalance(userId, activity.getPrice(), "join_activity", activityId.toString(), activity.getCreatorId());
        if (!success) {
            throw new RuntimeException("余额不足，无法报名");
        }

        // 分账逻辑
        // 根据红娘拉新人数确定分成比例：拉10人30%，拉50人33%，拉100人35%
        Long creatorId = activity.getCreatorId();
        Long referralCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getReferrerId, creatorId));
        
        double commissionRate = 0.3; // 默认分成比例
        if (referralCount >= 100) {
            commissionRate = 0.35;
        } else if (referralCount >= 50) {
            commissionRate = 0.33;
        } else if (referralCount >= 10) {
            commissionRate = 0.30;
        } else {
            // 虽然申请红娘需要10人，但此处做个兼容逻辑
            commissionRate = 0.1;
        }

        int commission = (int) (activity.getPrice() * commissionRate);
        walletService.addBalance(creatorId, commission, "activity_commission", activityId.toString(), userId);

        System.out.println("用户 " + userId + " 成功报名活动 " + activityId + "，红娘佣金: " + commission);
    }
}
