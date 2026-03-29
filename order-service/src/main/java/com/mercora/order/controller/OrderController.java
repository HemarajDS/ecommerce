package com.mercora.order.controller;

import com.mercora.order.dto.CreateOrderRequest;
import com.mercora.order.dto.OrderResponse;
import com.mercora.order.dto.ReturnRequestDto;
import com.mercora.order.dto.UpdateOrderStatusRequest;
import com.mercora.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order lifecycle and tracking APIs")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new order")
    public OrderResponse createOrder(@RequestHeader("X-User-Id") String userId, @Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(userId, request);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get an order by ID")
    public OrderResponse getOrder(@PathVariable String orderId) {
        return orderService.getOrder(orderId);
    }

    @GetMapping
    @Operation(summary = "List orders for the current user")
    public List<OrderResponse> listOrders(@RequestHeader("X-User-Id") String userId) {
        return orderService.getOrdersForUser(userId);
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "Update order status")
    public OrderResponse updateStatus(@PathVariable String orderId, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(orderId, request);
    }

    @PostMapping("/{orderId}/returns")
    @Operation(summary = "Request a return for an order")
    public OrderResponse requestReturn(@PathVariable String orderId, @Valid @RequestBody ReturnRequestDto request) {
        return orderService.requestReturn(orderId, request);
    }
}
