package com.example.smalltest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderCreateRequest(

        @NotBlank(message = "고객 이름은 필수입니다.")
        String customerName,

        @NotEmpty(message = "최소 1개 이상의 주문 항목")
        @Valid
        List<OrderItemRequest> orderItems
) {
}
