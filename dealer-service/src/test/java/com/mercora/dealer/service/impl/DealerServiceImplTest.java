package com.mercora.dealer.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mercora.dealer.config.DealerProperties;
import com.mercora.dealer.dto.CreatePurchaseOrderRequest;
import com.mercora.dealer.dto.DealerOnboardingRequest;
import com.mercora.dealer.dto.PurchaseOrderItemRequest;
import com.mercora.dealer.event.DealerEventPublisher;
import com.mercora.dealer.exception.BusinessRuleException;
import com.mercora.dealer.model.DealerDocument;
import com.mercora.dealer.model.DealerStatus;
import com.mercora.dealer.repository.DealerRepository;
import com.mercora.dealer.repository.LedgerEntryRepository;
import com.mercora.dealer.repository.PurchaseOrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class DealerServiceImplTest {

    @Mock
    private DealerRepository dealerRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private LedgerEntryRepository ledgerEntryRepository;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private DealerEventPublisher dealerEventPublisher;

    @InjectMocks
    private DealerServiceImpl dealerService;

    private DealerProperties dealerProperties;

    @BeforeEach
    void setUp() {
        dealerProperties = new DealerProperties();
        dealerProperties.setCreditLockSeconds(15);
        dealerProperties.setPaymentTermDays(30);
        dealerService = new DealerServiceImpl(
                dealerRepository,
                purchaseOrderRepository,
                ledgerEntryRepository,
                redisTemplate,
                dealerProperties,
                dealerEventPublisher);
    }

    @Test
    void onboardDealerShouldCreatePendingDealer() {
        when(dealerRepository.save(any(DealerDocument.class))).thenAnswer(invocation -> {
            DealerDocument dealer = invocation.getArgument(0);
            dealer.setId("dealer-1");
            return dealer;
        });

        var response = dealerService.onboardDealer(new DealerOnboardingRequest(
                "user-1", "Acme Distribution", "Ravi Patel", "ravi@example.com",
                BigDecimal.valueOf(100000), BigDecimal.valueOf(50000), 100));

        assertEquals(DealerStatus.PENDING, response.status());
        verify(dealerEventPublisher).publishDealerCreated(any(DealerDocument.class));
    }

    @Test
    void createPurchaseOrderShouldRejectQuotaOverflow() {
        DealerDocument dealer = new DealerDocument();
        dealer.setId("dealer-1");
        dealer.setStatus(DealerStatus.ACTIVE);
        dealer.setCreditLimit(BigDecimal.valueOf(100000));
        dealer.setMonthlyLimit(BigDecimal.valueOf(50000));
        dealer.setProductQuota(1);
        dealer.setCreditUsed(BigDecimal.ZERO);
        when(dealerRepository.findById("dealer-1")).thenReturn(Optional.of(dealer));

        assertThrows(BusinessRuleException.class, () -> dealerService.createPurchaseOrder(
                "dealer-1",
                new CreatePurchaseOrderRequest(List.of(
                        new PurchaseOrderItemRequest("prod-1", "SKU-1", 2, BigDecimal.valueOf(1000))))));
    }
}
