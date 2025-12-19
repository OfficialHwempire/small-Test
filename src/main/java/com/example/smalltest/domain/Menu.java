package com.example.smalltest.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "menus")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Boolean available;

    @Builder
    public Menu(String name, Integer price, Boolean available) {
        this.name = name;
        this.price = price;
        this.available = available != null ? available : true;
    }

    public void updateAvailability(boolean available) {
        this.available = available;
    }
}
