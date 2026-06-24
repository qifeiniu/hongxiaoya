package com.hongxiaoya.api.controller;

import com.hongxiaoya.api.common.Result;
import com.hongxiaoya.api.config.UserContext;
import com.hongxiaoya.api.entity.UserProfile;
import com.hongxiaoya.api.service.RelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/relation")
@RequiredArgsConstructor
public class RelationController {

    private final RelationService relationService;

    @PostMapping("/like")
    public Result<String> like(@RequestParam Long targetId) {
        Long userId = UserContext.getUserId();
        relationService.like(userId, targetId);
        return Result.success("操作成功");
    }

    @PostMapping("/dislike")
    public Result<String> dislike(@RequestParam Long targetId) {
        Long userId = UserContext.getUserId();
        relationService.dislike(userId, targetId);
        return Result.success("操作成功");
    }

    @PostMapping("/block")
    public Result<String> block(@RequestParam Long targetId) {
        Long userId = UserContext.getUserId();
        relationService.block(userId, targetId);
        return Result.success("已屏蔽");
    }

    @PostMapping("/unblock")
    public Result<String> unblock(@RequestParam Long targetId) {
        Long userId = UserContext.getUserId();
        relationService.unblock(userId, targetId);
        return Result.success("已解除屏蔽");
    }

    @GetMapping("/block-list")
    public Result<List<UserProfile>> getBlockList() {
        Long userId = UserContext.getUserId();
        return Result.success(relationService.getBlockList(userId));
    }

    @GetMapping("/who-likes-me")
    public Result<List<com.hongxiaoya.api.dto.WhoLikesMeDto>> whoLikesMe() {
        Long userId = UserContext.getUserId();
        return Result.success(relationService.getWhoLikesMe(userId));
    }

    @PostMapping("/unlock-who-likes-me")
    public Result<String> unlockWhoLikesMe(@RequestParam Long targetId) {
        Long userId = UserContext.getUserId();
        try {
            relationService.unlockWhoLikesMe(userId, targetId);
            return Result.success("解锁成功");
        } catch (Exception e) {
            return Result.error(2003, e.getMessage());
        }
    }

    @PostMapping("/unlock-wechat")
    public Result<String> unlockWechat(@RequestParam Long targetId) {
        Long userId = UserContext.getUserId();
        try {
            String wechatId = relationService.unlockWechat(userId, targetId);
            return Result.success(wechatId);
        } catch (Exception e) {
            return Result.error(2001, e.getMessage());
        }
    }

    @GetMapping("/visitors")
    public Result<List<com.hongxiaoya.api.dto.VisitorRecordDto>> getVisitors() {
        Long userId = UserContext.getUserId();
        return Result.success(relationService.getVisitors(userId));
    }

    @PostMapping("/unlock-visitor")
    public Result<String> unlockVisitor(@RequestParam Long visitorId) {
        Long userId = UserContext.getUserId();
        try {
            relationService.unlockVisitor(userId, visitorId);
            return Result.success("解锁成功");
        } catch (Exception e) {
            return Result.error(2002, e.getMessage());
        }
    }
}
