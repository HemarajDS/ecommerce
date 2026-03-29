package com.mercora.order.service.impl;

import com.mercora.order.dto.CreateOrderRequest;
import com.mercora.order.dto.OrderItemRequest;
import com.mercora.order.dto.OrderItemResponse;
import com.mercora.order.dto.OrderResponse;
import com.mercora.order.dto.OrderTimelineResponse;
import com.mercora.order.dto.ReturnRequestDto;
import com.mercora.order.dto.ReturnRequestResponse;
import com.mercora.order.dto.UpdateOrderStatusRequest;
import com.mercora.order.event.OrderEventPublisher;
import com.mercora.order.exception.BusinessRuleException;
import com.mercora.order.exception.ResourceNotFoundException;
import com.mercora.order.model.OrderDocument;
import com.mercora.order.model.OrderItem;
import com.mercora.order.model.OrderStatus;
import com.mercora.order.model.OrderTimelineEntry;
import com.mercora.order.model.ReturnRequestDetails;
import com.mercora.order.model.ReturnStatus;
import com.mercora.order.repository.OrderRepository;
import com.mercora.order.service.OrderService;
import com.mercora.order.websocket.OrderStatusPublisher;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        VALID_TRANSITIONS.put(OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.CONFIRMED, Set.of(OrderStatus.ALLOCATED, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.ALLOCATED, Set.of(OrderStatus.PACKED, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.PACKED, Set.of(OrderStatus.SHIPPED));
        VALID_TRANSITIONS.put(OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED));
        VALID_TRANSITIONS.put(OrderStatus.DELIVERED, Set.of(OrderStatus.RETURNED));
        VALID_TRANSITIONS.put(OrderStatus.RETURNED, Set.of());
        VALID_TRANSITIONS.put(OrderStatus.CANCELLED, Set.of());
    }

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;
    private final OrderStatusPublisher orderStatusPublisher;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderEventPublisher orderEventPublisher,
            OrderStatusPublisher orderStatusPublisher) {
        this.orderRepository = orderRepository;
        this.orderEventPublisher = orderEventPublisher;
        this.orderStatusPublisher = orderStatusPublisher;
    }

    @Override
    public OrderResponse createOrder(String userId, CreateOrderRequest request) {
        OrderDocument order = new OrderDocument();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setItems(request.items().stream().map(this::toItem).toList());
        order.setSubtotal(request.subtotal());
        order.setDiscountTotal(request.discountTotal());
        order.setGrandTotal(request.grandTotal());
        order.setCurrency(request.currency());
        order.setPaymentSessionId(request.paymentSessionId());
        order.setPaymentMethod(request.paymentMethod());
        order.setTimeline(new ArrayList<>(List.of(timeline(OrderStatus.PENDING, "Order created"))));
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        OrderDocument saved = orderRepository.save(order);
        orderEventPublisher.publishPlaced(saved);
        OrderResponse response = toResponse(saved);
        orderStatusPublisher.publish(response);
        return response;
    }

    @Override
    public OrderResponse getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @Override
    public List<OrderResponse> getOrdersForUser(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Override
    public OrderResponse updateStatus(String orderId, UpdateOrderStatusRequest request) {
        OrderDocument order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        transition(order, request.status(), request.note());
        OrderDocument saved = orderRepository.save(order);
        publishLifecycleEvent(saved);
        OrderResponse response = toResponse(saved);
        orderStatusPublisher.publish(response);
        return response;
    }

    @Override
    public OrderResponse requestReturn(String orderId, ReturnRequestDto request) {
        OrderDocument order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessRuleException("Returns can only be requested for delivered orders");
        }

        ReturnRequestDetails returnRequest = new ReturnRequestDetails();
        returnRequest.setStatus(ReturnStatus.REQUESTED);
        returnRequest.setReason(request.reason());
        returnRequest.setRequestedAt(Instant.now());
        returnRequest.setUpdatedAt(Instant.now());
        order.setReturnRequest(returnRequest);
        transition(order, OrderStatus.RETURNED, "Return requested");

        OrderDocument saved = orderRepository.save(order);
        OrderResponse response = toResponse(saved);
        orderStatusPublisher.publish(response);
        return response;
    }

    @Override
    public OrderResponse confirmOrderByPaymentSession(String paymentSessionId) {
        OrderDocument order = orderRepository.findByPaymentSessionId(paymentSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for payment session"));
        if (order.getStatus() == OrderStatus.PENDING) {
            transition(order, OrderStatus.CONFIRMED, "Payment confirmed");
            OrderDocument saved = orderRepository.save(order);
            OrderResponse response = toResponse(saved);
            orderStatusPublisher.publish(response);
            return response;
        }
        return toResponse(order);
    }

    private void transition(OrderDocument order, OrderStatus nextStatus, String note) {
        Set<OrderStatus> allowed = VALID_TRANSITIONS.getOrDefault(order.getStatus(), Set.of());
        if (!allowed.contains(nextStatus)) {
            throw new BusinessRuleException("Invalid order status transition from " + order.getStatus() + " to " + nextStatus);
        }
        order.setStatus(nextStatus);
        order.getTimeline().add(timeline(nextStatus, note == null || note.isBlank() ? "Status updated" : note));
        order.setUpdatedAt(Instant.now());
    }

    private void publishLifecycleEvent(OrderDocument order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            orderEventPublisher.publishCancelled(order);
        } else if (order.getStatus() == OrderStatus.DELIVERED) {
            orderEventPublisher.publishDelivered(order);
        }
    }

    private OrderItem toItem(OrderItemRequest request) {
        OrderItem item = new OrderItem();
        item.setProductId(request.productId());
        item.setSku(request.sku());
        item.setName(request.name());
        item.setQuantity(request.quantity());
        item.setUnitPrice(request.unitPrice());
        item.setLineTotal(request.lineTotal());
        return item;
    }

    private OrderTimelineEntry timeline(OrderStatus status, String note) {
        OrderTimelineEntry entry = new OrderTimelineEntry();
        entry.setStatus(status);
        entry.setNote(note);
        entry.setChangedAt(Instant.now());
        return entry;
    }

    private OrderResponse toResponse(OrderDocument order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getStatus(),
                order.getItems().stream()
                        .map(item -> new OrderItemResponse(
                                item.getProductId(),
                                item.getSku(),
                                item.getName(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getLineTotal()))
                        .toList(),
                order.getSubtotal(),
                order.getDiscountTotal(),
                order.getGrandTotal(),
                order.getCurrency(),
                order.getPaymentSessionId(),
                order.getPaymentMethod(),
                order.getReturnRequest() == null ? null : new ReturnRequestResponse(
                        order.getReturnRequest().getStatus(),
                        order.getReturnRequest().getReason(),
                        order.getReturnRequest().getRequestedAt(),
                        order.getReturnRequest().getUpdatedAt()),
                order.getTimeline().stream()
                        .map(entry -> new OrderTimelineResponse(entry.getStatus(), entry.getNote(), entry.getChangedAt()))
                        .toList(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
