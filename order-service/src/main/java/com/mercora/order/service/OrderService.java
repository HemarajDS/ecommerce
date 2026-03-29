package com.mercora.order.service;

import com.mercora.order.dto.CreateOrderRequest;
import com.mercora.order.dto.OrderResponse;
import com.mercora.order.dto.ReturnRequestDto;
import com.mercora.order.dto.UpdateOrderStatusRequest;
import java.util.List;

public interface OrderService {

    OrderResponse createOrder(String userId, CreateOrderRequest request);

    OrderResponse getOrder(String orderId);

    List<OrderResponse> getOrdersForUser(String userId);

    OrderResponse updateStatus(String orderId, UpdateOrderStatusRequest request);

    OrderResponse requestReturn(String orderId, ReturnRequestDto request);

    OrderResponse confirmOrderByPaymentSession(String paymentSessionId);
}
