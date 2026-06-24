package com.hongxiaoya.api.controller;

import com.hongxiaoya.api.common.Result;
import com.hongxiaoya.api.config.UserContext;
import com.hongxiaoya.api.dto.UpdateProfileReq;
import com.hongxiaoya.api.dto.RealNameAuthReq;
import com.hongxiaoya.api.entity.UserProfile;
import com.hongxiaoya.api.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/info")
    public Result<UserProfile> getInfo() {
        Long userId = UserContext.getUserId();
        return Result.success(profileService.getProfile(userId));
    }

    @PostMapping("/update")
    public Result<String> updateProfile(@RequestBody UpdateProfileReq req) {
        Long userId = UserContext.getUserId();
        profileService.updateProfile(userId, req);
        return Result.success("更新成功");
    }

    @PostMapping("/realNameAuth")
    public Result<String> realNameAuth(@Validated @RequestBody RealNameAuthReq req) {
        Long userId = UserContext.getUserId();
        profileService.realNameAuth(userId, req.getFrontImageUrl(), req.getBackImageUrl(), req.getFaceImageUrl());
        return Result.success("认证成功");
    }

    @GetMapping("/detail")
    public Result<UserProfile> getUserDetail(@RequestParam Long targetUserId) {
        Long userId = UserContext.getUserId();
        if (userId != null && !userId.equals(targetUserId)) {
            // Spring could use RelationService here or ProfileService could autowire RelationService
            // Let's just return the profile. We can add recordVisitor in ProfileService.
            return Result.success(profileService.getUserDetail(userId, targetUserId));
        }
        return Result.success(profileService.getProfile(targetUserId));
    }

    @PostMapping("/apply-matchmaker")
    public Result<String> applyMatchmaker() {
        Long userId = UserContext.getUserId();
        profileService.applyMatchmaker(userId);
        return Result.success("申请成功");
    }
}
