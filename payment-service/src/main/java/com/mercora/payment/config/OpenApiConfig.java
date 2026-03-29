package com.mercora.payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mercora Payment Service")
                        .version("v1")
                        .description("Payment intent, webhook, and refund APIs"));
    }
}
