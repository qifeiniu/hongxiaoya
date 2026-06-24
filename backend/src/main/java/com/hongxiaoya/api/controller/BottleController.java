package com.hongxiaoya.api.controller;

import com.hongxiaoya.api.common.Result;
import com.hongxiaoya.api.config.UserContext;
import com.hongxiaoya.api.service.BottleService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bottle")
@RequiredArgsConstructor
public class BottleController {

    private final BottleService bottleService;

    @PostMapping("/throw")
    public Result<String> throwBottle(@RequestBody ThrowReq req) {
        Long userId = UserContext.getUserId();
        bottleService.throwBottle(userId, req.getContent());
        return Result.success("抛出成功");
    }

    @GetMapping("/pick")
    public Result<Map<String, Object>> pickBottle() {
        Long userId = UserContext.getUserId();
        Map<String, Object> bottle = bottleService.pickBottle(userId);
        if (bottle == null) {
            return Result.success(null);
        }
        return Result.success(bottle);
    }

    @GetMapping("/records")
    public Result<List<Map<String, Object>>> getRecords(@RequestParam Integer type) {
        Long userId = UserContext.getUserId();
        List<Map<String, Object>> records = bottleService.getRecords(userId, type);
        return Result.success(records);
    }

    @Data
    public static class ThrowReq {
        private String content;
    }
}
