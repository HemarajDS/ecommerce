package com.mercora.dealer;

import com.mercora.dealer.config.DealerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DealerProperties.class)
public class DealerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DealerServiceApplication.class, args);
    }
}
