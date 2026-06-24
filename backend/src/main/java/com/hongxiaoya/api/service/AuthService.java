package com.hongxiaoya.api.service;

import com.hongxiaoya.api.dto.LoginReq;
import com.hongxiaoya.api.dto.WechatLoginReq;

import java.util.Map;

public interface AuthService {
    void sendSms(String phone);
    Map<String, Object> loginByPhone(LoginReq req);
    Map<String, Object> loginByWechat(WechatLoginReq req);
}
