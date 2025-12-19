package com.example.smalltest.service;

import com.example.smalltest.domain.Menu;
import com.example.smalltest.domain.Order;
import com.example.smalltest.domain.OrderItem;
import com.example.smalltest.domain.OrderStatus;
import com.example.smalltest.dto.OrderCreateRequest;
import com.example.smalltest.dto.OrderItemRequest;
import com.example.smalltest.dto.OrderResponse;
import com.example.smalltest.repository.MenuRepository;
import com.example.smalltest.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;



@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final S3FileService s3FileService;


    public OrderResponse createOrder(OrderCreateRequest orderCreateRequest, MultipartFile file) {

        //1. 주문 생성
        Order order = Order.builder()
                .customerName(orderCreateRequest.customerName())
                .build();

        //2. 주문 항목 추가
        for (OrderItemRequest orderItemRequest : orderCreateRequest.orderItems()) {
            Menu menu = getMenu(orderItemRequest.menuId());
            order.addOrderItem(menu, orderItemRequest.quantity());
        }
        //3. 주문 저장

        //AWS S3와 연동해서 프로필 파일을 전송하는 로직
        try {
            //이 url을 db에 회원정보와 함께 저장 하세용
            // 나중에 프론트에서 회원 정보를 요청할 때 url 도 같이 전달
            String url = s3FileService.uploadToS3Bucket(file);
        } catch (IOException e) {

            throw new RuntimeException(e);
        }

        Order saved = orderRepository.save(order);
        return OrderResponse.from(saved);
    }

    private Menu getMenu(Long menuId) {

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 메뉴 "+ menuId));
        if(!menu.getAvailable()) throw new IllegalStateException("주문할 수 없는 메뉴입니다." + menu.getName());
        return menu;
    }

    public OrderResponse getOrder(long l) {

        Order byIdWithItems = orderRepository.findByIdWithItems(l);
        if (byIdWithItems == null) throw new IllegalArgumentException("주문을 찾을 수 없습니다." + l);
        return OrderResponse.from(byIdWithItems);
    }

    public OrderResponse updateOrderStatus(long l, OrderStatus orderStatus) {

        Order order = orderRepository.findById(l).orElseThrow(IllegalArgumentException::new);
        order.updateStatus(orderStatus);
        Order updatedOrder = orderRepository.save(order);

        return OrderResponse.from(updatedOrder);
    }
}
