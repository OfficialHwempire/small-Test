package com.example.smalltest.domain;

import java.util.Set;

/**
 * 주문 상태
 */
public enum OrderStatus {
    PENDING,      // 대기
    CONFIRMED,    // 확인됨
    PREPARING,    // 준비 중
    COMPLETED,    // 완료
    CANCELLED;    // 취소됨

    /**
     * 특정 상태로 전환 가능한지 확인
     */
    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case PENDING -> Set.of(CONFIRMED, CANCELLED).contains(newStatus);
            case CONFIRMED -> Set.of(PREPARING, CANCELLED).contains(newStatus);
            case PREPARING -> Set.of(COMPLETED).contains(newStatus);
            case COMPLETED, CANCELLED -> false;
        };
    }
}