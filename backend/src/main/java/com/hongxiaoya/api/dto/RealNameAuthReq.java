package com.hongxiaoya.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RealNameAuthReq {
    @NotBlank(message = "身份证正面不能为空")
    private String frontImageUrl;
    @NotBlank(message = "身份证反面不能为空")
    private String backImageUrl;
    @NotBlank(message = "活体检测人脸不能为空")
    private String faceImageUrl;
}