package com.fred.pokehub.config;

import com.fred.pokehub.application.port.in.RegisterUserUseCase;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DemoUserProperties.class)
public class DemoUserConfig {

	@Bean
	ApplicationRunner demoUserSeeder(RegisterUserUseCase registerUser, DemoUserProperties properties) {
		return new DemoUserSeeder(registerUser, properties);
	}

}
