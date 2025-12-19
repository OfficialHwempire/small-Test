package com.example.smalltest.repository;

import com.example.smalltest.domain.Order;
import com.example.smalltest.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {

    /**
     * 고객 이름으로 주문 조회
     */
    List<Order> findByCustomerName(String customerName);

    /**
     * 주문 상태로 주문 조회
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * 주문 항목을 포함한 주문 조회 (N+1 문제 방지)
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    Order findByIdWithItems(Long id);
}
