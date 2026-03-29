package com.mercora.dealer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dealerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mercora Dealer Service")
                        .version("v1")
                        .description("Dealer onboarding, purchase order, approval, and ledger APIs"));
    }
}
