package com.hongxiaoya.api.controller;

import com.hongxiaoya.api.common.Result;
import com.hongxiaoya.api.dto.LoginReq;
import com.hongxiaoya.api.dto.WechatLoginReq;
import com.hongxiaoya.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sms/send")
    public Result<Void> sendSms(@RequestParam String phone) {
        try {
            authService.sendSms(phone);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(1001, e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Validated @RequestBody LoginReq req) {
        try {
            Map<String, Object> data = authService.loginByPhone(req);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(1001, e.getMessage());
        }
    }

    @PostMapping("/login/wechat")
    public Result<Map<String, Object>> loginByWechat(@Validated @RequestBody WechatLoginReq req) {
        try {
            Map<String, Object> data = authService.loginByWechat(req);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(1001, e.getMessage());
        }
    }
}
