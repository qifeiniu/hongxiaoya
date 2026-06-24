package com.hongxiaoya.api.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "wx")
public class WxProperties {
    private Miniapp miniapp = new Miniapp();
    private Pay pay = new Pay();

    @Data
    public static class Miniapp {
        private String appid;
        private String secret;
    }

    @Data
    public static class Pay {
        private String appId;
        private String mchId;
        private String mchKey;
        private String keyPath;
    }
}