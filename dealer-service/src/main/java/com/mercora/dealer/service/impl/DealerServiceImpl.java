package com.mercora.dealer.service.impl;

import com.mercora.dealer.config.DealerProperties;
import com.mercora.dealer.dto.ApprovePurchaseOrderRequest;
import com.mercora.dealer.dto.CreatePurchaseOrderRequest;
import com.mercora.dealer.dto.DealerOnboardingRequest;
import com.mercora.dealer.dto.DealerResponse;
import com.mercora.dealer.dto.LedgerEntryResponse;
import com.mercora.dealer.dto.PurchaseOrderItemRequest;
import com.mercora.dealer.dto.PurchaseOrderItemResponse;
import com.mercora.dealer.dto.PurchaseOrderResponse;
import com.mercora.dealer.event.DealerEventPublisher;
import com.mercora.dealer.exception.BusinessRuleException;
import com.mercora.dealer.exception.ResourceNotFoundException;
import com.mercora.dealer.model.DealerDocument;
import com.mercora.dealer.model.DealerStatus;
import com.mercora.dealer.model.LedgerEntryDocument;
import com.mercora.dealer.model.LedgerEntryType;
import com.mercora.dealer.model.PurchaseOrderDocument;
import com.mercora.dealer.model.PurchaseOrderItem;
import com.mercora.dealer.model.PurchaseOrderStatus;
import com.mercora.dealer.repository.DealerRepository;
import com.mercora.dealer.repository.LedgerEntryRepository;
import com.mercora.dealer.repository.PurchaseOrderRepository;
import com.mercora.dealer.service.DealerService;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class DealerServiceImpl implements DealerService {

    private final DealerRepository dealerRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final StringRedisTemplate redisTemplate;
    private final DealerProperties dealerProperties;
    private final DealerEventPublisher dealerEventPublisher;

    public DealerServiceImpl(
            DealerRepository dealerRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            LedgerEntryRepository ledgerEntryRepository,
            StringRedisTemplate redisTemplate,
            DealerProperties dealerProperties,
            DealerEventPublisher dealerEventPublisher) {
        this.dealerRepository = dealerRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.redisTemplate = redisTemplate;
        this.dealerProperties = dealerProperties;
        this.dealerEventPublisher = dealerEventPublisher;
    }

    @Override
    public DealerResponse onboardDealer(DealerOnboardingRequest request) {
        DealerDocument dealer = new DealerDocument();
        dealer.setDealerCode("DLR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        dealer.setUserId(request.userId());
        dealer.setCompanyName(request.companyName());
        dealer.setContactName(request.contactName());
        dealer.setEmail(request.email());
        dealer.setStatus(DealerStatus.PENDING);
        dealer.setCreditLimit(request.creditLimit());
        dealer.setMonthlyLimit(request.monthlyLimit());
        dealer.setProductQuota(request.productQuota());
        dealer.setCreditUsed(BigDecimal.ZERO);
        dealer.setCreatedAt(Instant.now());
        dealer.setUpdatedAt(Instant.now());

        DealerDocument saved = dealerRepository.save(dealer);
        dealerEventPublisher.publishDealerCreated(saved);
        return toDealerResponse(saved);
    }

    @Override
    public DealerResponse getDealer(String dealerId) {
        return dealerRepository.findById(dealerId)
                .map(this::toDealerResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));
    }

    @Override
    public PurchaseOrderResponse createPurchaseOrder(String dealerId, CreatePurchaseOrderRequest request) {
        DealerDocument dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));

        if (dealer.getStatus() == DealerStatus.SUSPENDED) {
            throw new BusinessRuleException("Dealer is suspended");
        }

        BigDecimal total = request.items().stream()
                .map(item -> item.dealerPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (request.items().stream().mapToInt(PurchaseOrderItemRequest::quantity).sum() > dealer.getProductQuota()) {
            throw new BusinessRuleException("Purchase order exceeds dealer product quota");
        }

        String lockKey = "dealer:credit:" + dealerId;
        boolean locked = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(
                lockKey, "1", Duration.ofSeconds(dealerProperties.getCreditLockSeconds())));
        if (!locked) {
            throw new BusinessRuleException("Dealer credit check is busy, please retry");
        }

        try {
            PurchaseOrderDocument po = new PurchaseOrderDocument();
            po.setDealerId(dealerId);
            po.setPoNumber("PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            po.setItems(request.items().stream().map(this::toPoItem).toList());
            po.setTotalAmount(total);
            po.setDueDate(Instant.now().plusSeconds(dealerProperties.getPaymentTermDays() * 86400L));
            po.setCreatedAt(Instant.now());
            po.setUpdatedAt(Instant.now());

            boolean requiresApproval = dealer.getCreditUsed().add(total).compareTo(dealer.getCreditLimit()) > 0
                    || total.compareTo(dealer.getMonthlyLimit()) > 0;

            if (requiresApproval) {
                po.setStatus(PurchaseOrderStatus.PENDING_APPROVAL);
                po.setApprovalReason("Credit or monthly limit exceeded");
                PurchaseOrderDocument saved = purchaseOrderRepository.save(po);
                dealerEventPublisher.publishPendingApproval(saved);
                return toPurchaseOrderResponse(saved);
            }

            po.setStatus(PurchaseOrderStatus.PLACED);
            PurchaseOrderDocument saved = purchaseOrderRepository.save(po);
            dealer.setCreditUsed(dealer.getCreditUsed().add(total));
            dealer.setUpdatedAt(Instant.now());
            dealerRepository.save(dealer);
            ledgerEntryRepository.save(ledgerEntry(dealerId, saved.getId(), LedgerEntryType.DEBIT, total, "Purchase order placed"));
            return toPurchaseOrderResponse(saved);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    public PurchaseOrderResponse approvePurchaseOrder(String purchaseOrderId, ApprovePurchaseOrderRequest request) {
        PurchaseOrderDocument po = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found"));
        if (po.getStatus() != PurchaseOrderStatus.PENDING_APPROVAL) {
            throw new BusinessRuleException("Only pending approvals can be approved");
        }

        DealerDocument dealer = dealerRepository.findById(po.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));

        po.setStatus(PurchaseOrderStatus.APPROVED);
        po.setApprovalReason(request.reason());
        po.setUpdatedAt(Instant.now());
        PurchaseOrderDocument savedPo = purchaseOrderRepository.save(po);

        dealer.setCreditUsed(dealer.getCreditUsed().add(po.getTotalAmount()));
        dealer.setUpdatedAt(Instant.now());
        dealerRepository.save(dealer);

        ledgerEntryRepository.save(ledgerEntry(dealer.getId(), savedPo.getId(), LedgerEntryType.DEBIT, savedPo.getTotalAmount(), "Purchase order approved"));
        return toPurchaseOrderResponse(savedPo);
    }

    @Override
    public PurchaseOrderResponse rejectPurchaseOrder(String purchaseOrderId, ApprovePurchaseOrderRequest request) {
        PurchaseOrderDocument po = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found"));
        if (po.getStatus() != PurchaseOrderStatus.PENDING_APPROVAL) {
            throw new BusinessRuleException("Only pending approvals can be rejected");
        }
        po.setStatus(PurchaseOrderStatus.REJECTED);
        po.setApprovalReason(request.reason());
        po.setUpdatedAt(Instant.now());
        return toPurchaseOrderResponse(purchaseOrderRepository.save(po));
    }

    @Override
    public List<PurchaseOrderResponse> listPurchaseOrders(String dealerId) {
        return purchaseOrderRepository.findByDealerIdOrderByCreatedAtDesc(dealerId).stream()
                .map(this::toPurchaseOrderResponse)
                .toList();
    }

    @Override
    public List<LedgerEntryResponse> getLedger(String dealerId) {
        return ledgerEntryRepository.findByDealerIdOrderByCreatedAtDesc(dealerId).stream()
                .map(this::toLedgerResponse)
                .toList();
    }

    private PurchaseOrderItem toPoItem(PurchaseOrderItemRequest request) {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setProductId(request.productId());
        item.setSku(request.sku());
        item.setQuantity(request.quantity());
        item.setDealerPrice(request.dealerPrice());
        item.setLineTotal(request.dealerPrice().multiply(BigDecimal.valueOf(request.quantity())));
        return item;
    }

    private LedgerEntryDocument ledgerEntry(String dealerId, String poId, LedgerEntryType type, BigDecimal amount, String description) {
        LedgerEntryDocument entry = new LedgerEntryDocument();
        entry.setDealerId(dealerId);
        entry.setPurchaseOrderId(poId);
        entry.setType(type);
        entry.setAmount(amount);
        entry.setDescription(description);
        entry.setCreatedAt(Instant.now());
        return entry;
    }

    private DealerResponse toDealerResponse(DealerDocument dealer) {
        return new DealerResponse(
                dealer.getId(),
                dealer.getDealerCode(),
                dealer.getUserId(),
                dealer.getCompanyName(),
                dealer.getContactName(),
                dealer.getEmail(),
                dealer.getStatus(),
                dealer.getCreditLimit(),
                dealer.getMonthlyLimit(),
                dealer.getProductQuota(),
                dealer.getCreditUsed(),
                dealer.getCreatedAt());
    }

    private PurchaseOrderResponse toPurchaseOrderResponse(PurchaseOrderDocument po) {
        return new PurchaseOrderResponse(
                po.getId(),
                po.getDealerId(),
                po.getPoNumber(),
                po.getStatus(),
                po.getItems().stream()
                        .map(item -> new PurchaseOrderItemResponse(
                                item.getProductId(),
                                item.getSku(),
                                item.getQuantity(),
                                item.getDealerPrice(),
                                item.getLineTotal()))
                        .toList(),
                po.getTotalAmount(),
                po.getApprovalReason(),
                po.getDueDate(),
                po.getCreatedAt());
    }

    private LedgerEntryResponse toLedgerResponse(LedgerEntryDocument entry) {
        return new LedgerEntryResponse(
                entry.getId(),
                entry.getDealerId(),
                entry.getPurchaseOrderId(),
                entry.getType(),
                entry.getAmount(),
                entry.getDescription(),
                entry.getCreatedAt());
    }
}
