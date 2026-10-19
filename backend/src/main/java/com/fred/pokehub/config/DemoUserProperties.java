package com.fred.pokehub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pokehub.demo-user")
public record DemoUserProperties(boolean enabled, String username, String password) {
}
