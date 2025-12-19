package com.example.smalltest.dto;

import com.example.smalltest.domain.Order;
import com.example.smalltest.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 응답 DTO
 */
@Getter
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private String customerName;
    private OrderStatus status;
    private List<OrderItemResponse> orderItems;
    private Integer totalPrice;
    private LocalDateTime orderedAt;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerName(order.getCustomerName())
                .status(order.getStatus())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .toList())
                .totalPrice(order.getTotalPrice())
                .orderedAt(order.getOrderedAt())
                .build();
    }
}