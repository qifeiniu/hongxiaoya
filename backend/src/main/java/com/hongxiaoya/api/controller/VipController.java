package com.hongxiaoya.api.controller;

import com.hongxiaoya.api.common.Result;
import com.hongxiaoya.api.config.UserContext;
import com.hongxiaoya.api.entity.VipMembership;
import com.hongxiaoya.api.service.VipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vip")
@RequiredArgsConstructor
public class VipController {

    private final VipService vipService;

    @GetMapping("/info")
    public Result<VipMembership> getInfo() {
        Long userId = UserContext.getUserId();
        return Result.success(vipService.getVipInfo(userId));
    }

    @PostMapping("/buy")
    public Result<String> buyVip(@RequestParam Integer months) {
        Long userId = UserContext.getUserId();
        // Assuming user pays via WeChat/Alipay, and this endpoint is called after success.
        // Or this deducts some balance if using eggs. But VIP usually uses money.
        // Just mock success here.
        vipService.buyVip(userId, 1, months);
        return Result.success("购买成功");
    }
}
