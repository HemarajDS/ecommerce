package com.mercora.payment.factory;

import com.mercora.payment.gateway.PaymentGatewayClient;
import com.mercora.payment.model.PaymentProvider;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PaymentGatewayFactory {

    private final Map<String, PaymentGatewayClient> gateways;

    public PaymentGatewayFactory(Map<String, PaymentGatewayClient> gateways) {
        this.gateways = gateways;
    }

    public PaymentGatewayClient getGateway(PaymentProvider provider) {
        PaymentGatewayClient client = gateways.get(provider.name());
        if (client == null) {
            throw new IllegalArgumentException("No gateway configured for provider " + provider);
        }
        return client;
    }
}
