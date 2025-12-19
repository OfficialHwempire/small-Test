package com.example.smalltest.controller;

import com.example.smalltest.dto.OrderCreateRequest;
import com.example.smalltest.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestPart("request") OrderCreateRequest orderCreateRequest
    , @RequestPart("file") MultipartFile file
                                         ){
        return ResponseEntity.ok(orderService.createOrder(orderCreateRequest,file));
    }

}
