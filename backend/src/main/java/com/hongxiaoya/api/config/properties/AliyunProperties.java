package com.hongxiaoya.api.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun")
public class AliyunProperties {
    private String accessKeyId;
    private String accessKeySecret;
    private Oss oss = new Oss();
    private Sms sms = new Sms();
    private Ocr ocr = new Ocr();
    private Facebody facebody = new Facebody();

    @Data
    public static class Oss {
        private String endpoint;
        private String bucketName;
    }

    @Data
    public static class Sms {
        private String signName;
        private String templateCode;
    }

    @Data
    public static class Ocr {
        private String endpoint;
    }

    @Data
    public static class Facebody {
        private String endpoint;
    }
}