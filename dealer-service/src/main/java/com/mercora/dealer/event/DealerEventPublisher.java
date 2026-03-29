package com.mercora.dealer.event;

import com.mercora.dealer.config.DealerProperties;
import com.mercora.dealer.model.DealerDocument;
import com.mercora.dealer.model.PurchaseOrderDocument;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DealerEventPublisher {

    private final KafkaTemplate<String, DealerEvent> kafkaTemplate;
    private final DealerProperties dealerProperties;

    public DealerEventPublisher(KafkaTemplate<String, DealerEvent> kafkaTemplate, DealerProperties dealerProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.dealerProperties = dealerProperties;
    }

    public void publishDealerCreated(DealerDocument dealer) {
        kafkaTemplate.send(
                dealerProperties.getDealerCreatedTopic(),
                dealer.getId(),
                new DealerEvent("dealer.created", dealer.getId(), dealer.getDealerCode(), java.time.Instant.now()));
    }

    public void publishPendingApproval(PurchaseOrderDocument purchaseOrder) {
        kafkaTemplate.send(
                dealerProperties.getPoPendingApprovalTopic(),
                purchaseOrder.getId(),
                new DealerEvent("dealer.po.pending-approval", purchaseOrder.getDealerId(), purchaseOrder.getPoNumber(), java.time.Instant.now()));
    }
}
