package com.example.smalltest.dto;

import com.example.smalltest.domain.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 주문 항목 응답 DTO
 */
@Getter
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Long menuId;
    private String menuName;
    private Integer menuPrice;
    private Integer quantity;
    private Integer subtotal;

    public static OrderItemResponse from(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .menuId(orderItem.getMenu().getId())
                .menuName(orderItem.getMenuName())
                .menuPrice(orderItem.getMenuPrice())
                .quantity(orderItem.getQuantity())
                .subtotal(orderItem.getMenuPrice() * orderItem.getQuantity())
                .build();
    }
}