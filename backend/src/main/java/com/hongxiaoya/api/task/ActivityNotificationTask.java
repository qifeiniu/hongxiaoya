package com.hongxiaoya.api.task;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hongxiaoya.api.entity.Activity;
import com.hongxiaoya.api.entity.EggLedger;
import com.hongxiaoya.api.mapper.ActivityMapper;
import com.hongxiaoya.api.mapper.EggLedgerMapper;
import com.hongxiaoya.api.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityNotificationTask {

    private final ActivityMapper activityMapper;
    private final EggLedgerMapper eggLedgerMapper;
    private final MessageService messageService;

    // 每分钟执行一次
    @Scheduled(cron = "0 * * * * ?")
    public void notifyActivityStart() {
        log.info("开始执行相亲活动系统通知定时任务...");
        Date now = new Date();

        // 1. 提前1小时通知（距开始60分钟 ~ 61分钟）
        Date oneHourStart = DateUtil.offsetMinute(now, 60);
        Date oneHourEnd = DateUtil.offsetMinute(now, 61);
        notifyForTimeWindow(oneHourStart, oneHourEnd, "1小时");

        // 2. 提前10分钟通知（距开始10分钟 ~ 11分钟）
        Date tenMinStart = DateUtil.offsetMinute(now, 10);
        Date tenMinEnd = DateUtil.offsetMinute(now, 11);
        notifyForTimeWindow(tenMinStart, tenMinEnd, "10分钟");
    }

    private void notifyForTimeWindow(Date start, Date end, String timeStr) {
        // 查找在时间窗口内且状态为报名中（0）的活动
        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Activity::getStatus, 0)
                .ge(Activity::getStartTime, start)
                .lt(Activity::getStartTime, end);

        List<Activity> activities = activityMapper.selectList(queryWrapper);
        if (activities == null || activities.isEmpty()) {
            return;
        }

        for (Activity activity : activities) {
            // 通过 EggLedger 查找报名了该活动的用户
            LambdaQueryWrapper<EggLedger> ledgerQuery = new LambdaQueryWrapper<>();
            ledgerQuery.eq(EggLedger::getScene, "join_activity")
                    .eq(EggLedger::getRelatedId, String.valueOf(activity.getId()));

            List<EggLedger> ledgers = eggLedgerMapper.selectList(ledgerQuery);
            if (ledgers != null && !ledgers.isEmpty()) {
                // 去重用户ID
                List<Long> userIds = ledgers.stream()
                        .map(EggLedger::getUserId)
                        .distinct()
                        .collect(Collectors.toList());

                for (Long userId : userIds) {
                    try {
                        // 发送系统通知 (msgType = 6)
                        String content = "您报名的相亲活动【" + activity.getName() + "】还有" + timeStr + "即将开始，请准时进入直播间进行相亲。";
                        messageService.sendMessage(0L, userId, 6, content);
                        log.info("已发送活动提前{}通知, 活动ID: {}, 用户ID: {}", timeStr, activity.getId(), userId);
                    } catch (Exception e) {
                        log.error("发送活动系统通知失败, 活动ID: {}, 用户ID: {}", activity.getId(), userId, e);
                    }
                }
            }
        }
    }
}
