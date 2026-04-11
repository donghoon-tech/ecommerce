package com.mall.order.service;

import com.mall.cart.repository.CartRepository;
import com.mall.cart.service.CartService;
import com.mall.order.domain.Order;
import com.mall.order.domain.OrderStatus;
import com.mall.order.repository.OrderRepository;
import com.mall.product.domain.Inventory;
import com.mall.product.domain.Product;
import com.mall.product.domain.Sku;
import com.mall.product.repository.InventoryRepository;
import com.mall.product.repository.ProductRepository;
import com.mall.product.repository.SkuRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SkuRepository skuRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    private Sku testSku;

    @BeforeEach
    void setUp() {
        Product product = Product.builder()
                .name("Order Test Product")
                .basePrice(new BigDecimal("10000"))
                .attributes(Map.of())
                .build();
        
        testSku = Sku.builder()
                .skuCode("ORDER-TEST-" + System.nanoTime())
                .attributes(Map.of())
                .additionalPrice(BigDecimal.ZERO)
                .build();
        product.addSku(testSku);
        productRepository.save(product);

        inventoryRepository.save(Inventory.builder()
                .sku(testSku)
                .stockQuantity(10)
                .build());
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        inventoryRepository.deleteAll();
        cartRepository.deleteAll();
        skuRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("정상적인 주문 생성 시 재고가 차감되고 주문이 저장된다")
    void createOrderSuccess() {
        // Given
        Long userId = 1L;
        cartService.addItem(userId, null, testSku.getId(), 2);

        // When
        Long orderId = orderService.createOrder(userId);

        // Then
        Order found = orderRepository.findWithItemsById(orderId).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(found.getOrderItems()).hasSize(1);
        
        Inventory inventory = inventoryRepository.findBySkuId(testSku.getId()).orElseThrow();
        assertThat(inventory.getStockQuantity()).isEqualTo(8);
    }

    @Test
    @DisplayName("재고가 부족하면 주문 생성이 실패한다")
    void createOrderFailDueToStock() {
        // Given
        Long userId = 1L;
        cartService.addItem(userId, null, testSku.getId(), 11);

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(userId))
                .isInstanceOf(IllegalStateException.class);
        
        // 주문이 저장되지 않았어야 함
        assertThat(orderRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("주문을 취소하면 재고가 원복되고 주문 상태가 CANCELLED로 변경된다")
    void cancelOrderAndRestoreStock() {
        // Given
        Long userId = 1L;
        cartService.addItem(userId, null, testSku.getId(), 3);
        Long orderId = orderService.createOrder(userId);
        
        Inventory inventoryAfterOrder = inventoryRepository.findBySkuId(testSku.getId()).orElseThrow();
        assertThat(inventoryAfterOrder.getStockQuantity()).isEqualTo(7);

        // When
        orderService.cancelOrder(orderId);

        // Then
        Order found = orderRepository.findById(orderId).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        
        Inventory inventoryAfterCancel = inventoryRepository.findBySkuId(testSku.getId()).orElseThrow();
        assertThat(inventoryAfterCancel.getStockQuantity()).isEqualTo(10);
    }
}
