package com.example.smalltest.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private Integer totalPrice;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    @Builder
    public Order(String customerName) {
        this.customerName = customerName;
        this.status = OrderStatus.PENDING;
        this.totalPrice = 0;
        this.orderedAt = LocalDateTime.now();
    }

    public void addOrderItem(Menu menu, Integer quantity){
        OrderItem orderItem = OrderItem.builder()
                .order(this)
                .menu(menu)
                .menuName(menu.getName())
                .menuPrice(menu.getPrice())
                .quantity(quantity)
                .build();

        this.orderItems.add(orderItem);
        this.totalPrice += menu.getPrice() * quantity;

    }

    public void updateStatus(OrderStatus orderStatus) {
        validateStatusTransition(orderStatus);
        this.status = orderStatus;
    }
    private void validateStatusTransition(OrderStatus newStatus) {
        if(!this.status.canTransitionTo(newStatus)){
            throw new IllegalStateException(
                    String.format("주문 상태를 %s에서 %s로 변경할 수 없습니다",this.status,newStatus)
            );
        }

    }
}
