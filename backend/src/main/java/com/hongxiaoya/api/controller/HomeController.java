package com.hongxiaoya.api.controller;

import com.hongxiaoya.api.common.Result;
import com.hongxiaoya.api.config.UserContext;
import com.hongxiaoya.api.entity.UserProfile;
import com.hongxiaoya.api.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/recommend")
    public Result<List<UserProfile>> getRecommendList() {
        Long userId = UserContext.getUserId();
        return Result.success(homeService.getRecommendList(userId));
    }

    @PostMapping("/send-rose")
    public Result<String> sendRose(@RequestParam Long targetUserId) {
        Long userId = UserContext.getUserId();
        try {
            homeService.sendRose(userId, targetUserId);
            return Result.success("送花成功，已开启会话");
        } catch (Exception e) {
            return Result.error(2001, e.getMessage());
        }
    }

    @PostMapping("/like")
    public Result<String> like(@RequestParam Long targetUserId) {
        Long userId = UserContext.getUserId();
        homeService.like(userId, targetUserId);
        return Result.success("已喜欢");
    }

    @PostMapping("/dislike")
    public Result<String> dislike(@RequestParam Long targetUserId) {
        Long userId = UserContext.getUserId();
        homeService.dislike(userId, targetUserId);
        return Result.success("已跳过");
    }

    @PostMapping("/unlock-wechat")
    public Result<String> unlockWechat(@RequestParam Long targetUserId) {
        Long userId = UserContext.getUserId();
        try {
            String wechatId = homeService.unlockWechat(userId, targetUserId);
            return Result.success(wechatId);
        } catch (Exception e) {
            return Result.error(2002, e.getMessage());
        }
    }
}
