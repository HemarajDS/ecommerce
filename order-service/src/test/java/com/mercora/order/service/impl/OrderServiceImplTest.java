package com.mercora.order.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mercora.order.dto.CreateOrderRequest;
import com.mercora.order.dto.OrderItemRequest;
import com.mercora.order.dto.UpdateOrderStatusRequest;
import com.mercora.order.event.OrderEventPublisher;
import com.mercora.order.exception.BusinessRuleException;
import com.mercora.order.model.OrderDocument;
import com.mercora.order.model.OrderStatus;
import com.mercora.order.repository.OrderRepository;
import com.mercora.order.websocket.OrderStatusPublisher;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderEventPublisher orderEventPublisher;
    @Mock
    private OrderStatusPublisher orderStatusPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrderShouldPersistPendingOrder() {
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest("prod-1", "SKU-1", "Trail Shoes", 1, BigDecimal.valueOf(1999), BigDecimal.valueOf(1999))),
                BigDecimal.valueOf(1999),
                BigDecimal.ZERO,
                BigDecimal.valueOf(1999),
                "INR",
                "session-1",
                "RAZORPAY");

        when(orderRepository.save(any(OrderDocument.class))).thenAnswer(invocation -> {
            OrderDocument order = invocation.getArgument(0);
            order.setId("order-1");
            return order;
        });

        var response = orderService.createOrder("user-1", request);

        assertEquals(OrderStatus.PENDING, response.status());
        verify(orderEventPublisher).publishPlaced(any(OrderDocument.class));
    }

    @Test
    void updateStatusShouldRejectInvalidTransition() {
        OrderDocument order = new OrderDocument();
        order.setId("order-1");
        order.setStatus(OrderStatus.PENDING);
        order.setTimeline(new java.util.ArrayList<>());
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

        assertThrows(BusinessRuleException.class, () ->
                orderService.updateStatus("order-1", new UpdateOrderStatusRequest(OrderStatus.SHIPPED, "Skip ahead")));
    }
}
