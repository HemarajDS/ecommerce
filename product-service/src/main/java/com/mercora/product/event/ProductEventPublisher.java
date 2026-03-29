package com.mercora.product.event;

import com.mercora.product.config.ProductProperties;
import com.mercora.product.model.ProductDocument;
import java.time.Instant;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductEventPublisher {

    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;
    private final ProductProperties productProperties;

    public ProductEventPublisher(KafkaTemplate<String, ProductEvent> kafkaTemplate, ProductProperties productProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.productProperties = productProperties;
    }

    public void publishCreated(ProductDocument product) {
        kafkaTemplate.send(productProperties.getProductCreatedTopic(), product.getId(), event("product.created", product));
    }

    public void publishUpdated(ProductDocument product) {
        kafkaTemplate.send(productProperties.getProductUpdatedTopic(), product.getId(), event("product.updated", product));
    }

    private ProductEvent event(String eventType, ProductDocument product) {
        return new ProductEvent(
                eventType,
                product.getId(),
                product.getSlug(),
                product.getName(),
                product.getBrandId(),
                product.getCategoryId(),
                product.getStatus(),
                Instant.now());
    }
}
