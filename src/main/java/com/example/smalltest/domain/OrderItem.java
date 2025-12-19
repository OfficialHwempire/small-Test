package com.example.smalltest.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 항목 엔티티
 */
@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false)
    private String menuName;

    @Column(nullable = false)
    private Integer menuPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Builder
    public OrderItem(Order order, Menu menu, String menuName, Integer menuPrice, Integer quantity) {
        this.order = order;
        this.menu = menu;
        this.menuName = menuName;
        this.menuPrice = menuPrice;
        this.quantity = quantity;
    }
}