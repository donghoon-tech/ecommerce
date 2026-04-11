package com.mall.order.repository;

import com.mall.order.domain.Order;
import com.mall.order.domain.OrderItem;
import com.mall.order.domain.OrderStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("주문과 주문 항목을 함께 저장하고 조회할 수 있다")
    void saveAndFindOrderWithItems() {
        // Given
        Order order = Order.builder()
                .userId(1L)
                .status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(new BigDecimal("30000"))
                .build();

        OrderItem item1 = OrderItem.builder()
                .skuId(1L)
                .skuName("Item 1")
                .price(new BigDecimal("10000"))
                .quantity(1)
                .build();

        OrderItem item2 = OrderItem.builder()
                .skuId(2L)
                .skuName("Item 2")
                .price(new BigDecimal("10000"))
                .quantity(2)
                .build();

        order.addOrderItem(item1);
        order.addOrderItem(item2);

        // When
        Order savedOrder = orderRepository.save(order);
        em.flush();
        em.clear();

        // Then
        Order found = orderRepository.findById(savedOrder.getId()).orElseThrow();
        assertThat(found.getUserId()).isEqualTo(1L);
        assertThat(found.getOrderItems()).hasSize(2);
        assertThat(found.getTotalAmount()).isEqualByComparingTo("30000");
    }
}
