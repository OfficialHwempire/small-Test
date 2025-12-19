package com.example.smalltest.service;

import com.example.smalltest.domain.Menu;
import com.example.smalltest.domain.Order;
import com.example.smalltest.domain.OrderStatus;
import com.example.smalltest.dto.OrderCreateRequest;
import com.example.smalltest.dto.OrderItemRequest;
import com.example.smalltest.dto.OrderResponse;
import com.example.smalltest.repository.MenuRepository;
import com.example.smalltest.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.matchers.Any;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService를 테스트하는 것이야")
public class OrderServiceTest {

    private Menu americano;
    private Menu latte;
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {

        americano = Menu.builder()
                .name("americano")
                .price(1000)
                .available(true)
                .build();
        latte = Menu.builder()
                .name("라떼")
                .price(4500)
                .available(true)
                .build();

    }
    @Nested
    @DisplayName("주문 생성")
    class CreateOrder{

        @Test
        @DisplayName("성공: 단일 메뉴를 주문할 수 있다")
        void createOrder_WithSingleMunu_Success(){
         //given
            when(menuRepository.findById(1L))
                    .thenReturn(Optional.of(americano));

            //orderRepository가 주문을 save하면 save한 내용 그대로 반환하는 설정
            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(invocation ->{

                        Order order = invocation.getArgument(0);
                        return order;
                    });

            //주문 요청 DTO 생성
            OrderItemRequest orderItemRequest = new OrderItemRequest(1L, 3);
            OrderCreateRequest orderCreateRequest = new OrderCreateRequest("김춘식", List.of(orderItemRequest));

            //when
            OrderResponse response = orderService.createOrder(orderCreateRequest);

            //then
            assertThat(response).isNotNull();
            assertThat(response.getOrderItems()).hasSize(1);
            assertThat(response.getCustomerName()).isEqualTo("김춘식");
            assertThat(response.getTotalPrice()).isEqualTo(3000);
            assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);

            verify(menuRepository,times(1)).findById(1L);
            verify(orderRepository,times(1)).save(any(Order.class));


        }

        @Test
        @DisplayName("성공: 여러 메뉴를 주문할 수 있다")
        void createOrder_WithMultiMunu_Success(){

            //given
            when(menuRepository.findById(1L))
                    .thenReturn(Optional.of(americano));

            when(menuRepository.findById(2L))
            .thenReturn(Optional.of(latte));

            //orderRepository가 주문을 save하면 save한 내용 그대로 반환하는 설정
            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(invocation ->invocation.getArgument(0));
            //주문 요청 DTO 생성
            OrderItemRequest orderItemRequest = new OrderItemRequest(1L, 3);
            OrderItemRequest orderItemRequest2 = new OrderItemRequest(2L, 2);
            OrderCreateRequest orderCreateRequest = new OrderCreateRequest("김춘식", List.of(orderItemRequest
                    , orderItemRequest2));

            //when
            OrderResponse response = orderService.createOrder(orderCreateRequest);

            //then
            assertThat(response).isNotNull();
            assertThat(response.getOrderItems()).hasSize(2);
            assertThat(response.getCustomerName()).isEqualTo("김춘식");
            assertThat(response.getTotalPrice()).isEqualTo(12000);
            assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);

            verify(menuRepository,times(1)).findById(1L);
            verify(orderRepository,times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("실패 : 존재하지 않는 메뉴라면 주문이 실패해야 한다")
        void createOrder_WithNonExistMenu_ThrowsException(){

            //given
            when(menuRepository.findById(999L))
                    .thenReturn(Optional.empty());

            //주문 요청 DTO 생성
            OrderItemRequest orderItemRequest = new OrderItemRequest(999L, 3);
            OrderCreateRequest orderCreateRequest = new OrderCreateRequest("김춘식", List.of(orderItemRequest));

            //when

            //then
            assertThatThrownBy(() -> orderService.createOrder(orderCreateRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("존재하지");

            verify(orderRepository,never()).save(any(Order.class));
        }




    }
    @Nested
    @DisplayName("주문 조회")
    class getOrder{

        @Test
        @DisplayName("성공: 주문 ID로 조회")
        void getOrder_WithValidId_Success() {
            // Given
            Order order = Order.builder()
                    .customerName("홍길동")
                    .build();
            order.addOrderItem(americano, 2);

            when(orderRepository.findByIdWithItems(1L)).thenReturn(order);

            // When
            OrderResponse response = orderService.getOrder(1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getCustomerName()).isEqualTo("홍길동");
            assertThat(response.getOrderItems()).hasSize(1);

            verify(orderRepository, times(1)).findByIdWithItems(1L);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 주문")
        void getOrder_WithNonExistentId_ThrowsException() {
            // Given
            when(orderRepository.findByIdWithItems(999L)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> orderService.getOrder(999L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("주문 상태 변경")
    class UpdateOrderStatus {

        @Test
        @DisplayName("성공: PENDING -> CONFIRMED")
        void updateOrderStatus_PendingToConfirmed_Success() {
            // Given
            Order order = Order.builder()
                    .customerName("홍길동")
                    .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            OrderResponse response = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

            // Then
            assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            verify(orderRepository, times(1)).save(order);
        }

        @Test
        @DisplayName("성공: CONFIRMED -> PREPARING -> COMPLETED")
        void updateOrderStatus_FullFlow_Success() {
            // Given
            Order order = Order.builder()
                    .customerName("홍길동")
                    .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When & Then
            OrderResponse response1 = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);
            assertThat(response1.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

            OrderResponse response2 = orderService.updateOrderStatus(1L, OrderStatus.PREPARING);
            assertThat(response2.getStatus()).isEqualTo(OrderStatus.PREPARING);

            OrderResponse response3 = orderService.updateOrderStatus(1L, OrderStatus.COMPLETED);
            assertThat(response3.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("실패: 잘못된 상태 전환 (PENDING -> COMPLETED)")
        void updateOrderStatus_InvalidTransition_ThrowsException() {
            // Given
            Order order = Order.builder()
                    .customerName("홍길동")
                    .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            // When & Then
            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.COMPLETED))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("주문 상태를");

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("실패: 완료된 주문의 상태 변경 시도")
        void updateOrderStatus_CompletedOrder_ThrowsException() {
            // Given
            Order order = Order.builder()
                    .customerName("홍길동")
                    .build();
            order.updateStatus(OrderStatus.CONFIRMED);
            order.updateStatus(OrderStatus.PREPARING);
            order.updateStatus(OrderStatus.COMPLETED);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            // When & Then
            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.PENDING))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
