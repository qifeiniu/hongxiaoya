package com.hongxiaoya.api.controller;

import com.hongxiaoya.api.common.Result;
import com.hongxiaoya.api.config.UserContext;
import com.hongxiaoya.api.entity.Activity;
import com.hongxiaoya.api.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping("/list")
    public Result<List<Activity>> getList() {
        return Result.success(activityService.getActivityList());
    }

    @PostMapping("/create")
    public Result<String> create(@RequestBody Activity activity) {
        Long userId = UserContext.getUserId();
        activityService.createActivity(userId, activity);
        return Result.success("创建成功");
    }

    @PostMapping("/join")
    public Result<String> join(@RequestParam Long activityId) {
        Long userId = UserContext.getUserId();
        activityService.joinActivity(userId, activityId);
        return Result.success("报名成功");
    }
}
