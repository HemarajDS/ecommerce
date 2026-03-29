package com.mercora.cms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cmsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mercora CMS Service")
                        .version("v1")
                        .description("CMS page, section, SEO, and publishing APIs"));
    }
}
