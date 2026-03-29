package com.mercora.cart.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cartOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mercora Cart Service")
                        .version("v1")
                        .description("Cart, coupon, and checkout APIs for Mercora Commerce"));
    }
}
