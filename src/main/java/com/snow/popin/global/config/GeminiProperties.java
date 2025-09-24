package com.snow.popin.global.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "ai.gemini")
public class GeminiProperties {

    private Api api = new Api();
    private Integer timeout;

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    @Getter
    public static class Api {
        private String key;
        private String url;

        public void setKey(String key) {
            this.key = key;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}