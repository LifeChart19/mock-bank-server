package com.example.mockbank.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mock Bank API")
                        .description("LifeChart 프로젝트용 목 은행 서버 API 명세")
                        .version("1.0.0"));
    }
}
