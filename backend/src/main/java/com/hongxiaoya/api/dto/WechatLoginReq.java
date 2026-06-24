package com.hongxiaoya.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WechatLoginReq {
    @NotBlank(message = "微信授权code不能为空")
    private String code;
}