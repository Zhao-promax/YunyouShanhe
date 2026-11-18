package com.yunyou.shanhe.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档信息（SpringDoc + Knife4j）。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("云游山河 API")
                        .version("1.0.0")
                        .description("景区预约候补与目的地攻略平台后端接口"));
    }
}
