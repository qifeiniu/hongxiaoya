package com.hongxiaoya.api.controller;

import com.hongxiaoya.api.common.Result;
import com.hongxiaoya.api.config.UserContext;
import com.hongxiaoya.api.dto.ConversationDto;
import com.hongxiaoya.api.entity.Message;
import com.hongxiaoya.api.service.MessageService;
import com.hongxiaoya.api.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final WalletService walletService;

    @GetMapping("/conversations")
    public Result<List<ConversationDto>> getConversations() {
        Long userId = UserContext.getUserId();
        return Result.success(messageService.getConversations(userId));
    }

    @GetMapping("/history")
    public Result<List<Message>> getChatHistory(@RequestParam Long targetId) {
        Long userId = UserContext.getUserId();
        return Result.success(messageService.getChatHistory(userId, targetId));
    }

    @PostMapping("/send")
    public Result<Message> sendMessage(@RequestParam Long receiverId, @RequestParam Integer msgType, @RequestParam String content) {
        Long userId = UserContext.getUserId();
        return Result.success(messageService.sendMessage(userId, receiverId, msgType, content));
    }

    @PostMapping("/read")
    public Result<String> readMessage(@RequestParam Long targetId) {
        Long userId = UserContext.getUserId();
        messageService.readMessage(userId, targetId);
        return Result.success("已读");
    }

    @PostMapping("/top")
    public Result<String> topConversation(@RequestParam Long targetId, @RequestParam Boolean isTop) {
        Long userId = UserContext.getUserId();
        messageService.topConversation(userId, targetId, isTop);
        return Result.success("操作成功");
    }

    @PostMapping("/send-gift")
    public Result<String> sendGift(@RequestParam Long targetUserId, @RequestParam String giftType) {
        Long userId = UserContext.getUserId();
        int cost = 0;
        switch (giftType) {
            case "rose": cost = 20; break;
            case "car": cost = 50; break;
            case "rocket": cost = 100; break;
            default: return Result.error(400, "未知礼物类型");
        }

        boolean success = walletService.deductBalance(userId, cost, "send_gift_" + giftType, null, targetUserId);
        if (!success) {
            return Result.error(2003, "鸭蛋余额不足");
        }

        return Result.success("赠送成功");
    }

    @PostMapping("/video-call/start")
    public Result<String> startVideoCall(@RequestParam Long targetUserId) {
        Long userId = UserContext.getUserId();
        // 发起视频前预扣除前两分钟的费用 (60鸭蛋)
        boolean success = walletService.deductBalance(userId, 60, "video_call_prepay", null, targetUserId);
        if (!success) {
            return Result.error(2004, "鸭蛋余额不足，无法发起视频通话");
        }

        return Result.success("预扣费成功，允许呼叫");
    }

    @PostMapping("/video-call/heartbeat")
    public Result<String> videoCallHeartbeat(@RequestParam Long targetUserId, @RequestParam Integer minutes) {
        Long userId = UserContext.getUserId();
        // 每过2分钟，调用一次此接口扣费，minutes 是本次心跳对应的计费时长（比如 2）
        int cost = (minutes / 2) * 60;
        boolean success = walletService.deductBalance(userId, cost, "video_call_heartbeat", null, targetUserId);
        if (!success) {
            return Result.error(2005, "鸭蛋余额不足，即将挂断");
        }

        return Result.success("扣费成功");
    }
}