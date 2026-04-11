package com.mall.order.service;

import com.mall.cart.dto.CartResponse;
import com.mall.cart.service.CartService;
import com.mall.order.domain.Order;
import com.mall.order.domain.OrderItem;
import com.mall.order.domain.OrderStatus;
import com.mall.order.repository.OrderRepository;
import com.mall.product.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final InventoryService inventoryService;

    /**
     * 주문 생성 (재고 선점 포함)
     */
    @Transactional
    public Long createOrder(Long userId) {
        // 1. 장바구니 아이템 조회
        List<CartResponse> cartItems = cartService.getCartItems(userId, null);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("장바구니가 비어 있습니다.");
        }

        // 2. 주문 총액 계산
        BigDecimal totalAmount = cartItems.stream()
                .map(CartResponse::subTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 주문 엔티티 생성
        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(totalAmount)
                .build();

        // 4. 주문 항목 추가 및 재고 차감 (선점)
        for (CartResponse item : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .skuId(item.skuId())
                    .skuName(item.productName() + " (" + item.skuCode() + ")")
                    .price(item.price())
                    .quantity(item.quantity())
                    .build();
            order.addOrderItem(orderItem);

            // Phase 3 재고 선점 호출
            inventoryService.decreaseStock(item.skuId(), item.quantity());
        }

        // 5. 주문 저장 및 장바구니 비우기
        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(userId);

        log.info("Order created successfully. OrderId: {}, User: {}", savedOrder.getId(), userId);
        return savedOrder.getId();
    }

    /**
     * 주문 취소 (재고 복구 포함)
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Order ID"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        order.updateStatus(OrderStatus.CANCELLED);

        // 재고 복구
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.increaseStock(item.getSkuId(), item.getQuantity());
        }

        log.info("Order cancelled and stock restored. OrderId: {}", orderId);
    }
}
