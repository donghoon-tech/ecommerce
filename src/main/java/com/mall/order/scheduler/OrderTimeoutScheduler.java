package com.mall.order.scheduler;

import com.mall.order.domain.Order;
import com.mall.order.domain.OrderStatus;
import com.mall.order.repository.OrderRepository;
import com.mall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderTimeoutScheduler {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    /**
     * 결제 대기 중인 주문 중 10분이 경과한 주문을 자동으로 취소
     */
    @Scheduled(fixedDelay = 60000) // 1분마다 실행
    public void cancelTimeoutOrders() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(10);
        
        List<Order> timeoutOrders = orderRepository.findByStatusAndOrderDateBefore(
                OrderStatus.PENDING_PAYMENT, timeoutThreshold);

        if (!timeoutOrders.isEmpty()) {
            log.info("Found {} timeout orders to cancel", timeoutOrders.size());
            for (Order order : timeoutOrders) {
                try {
                    orderService.cancelOrder(order.getId());
                } catch (Exception e) {
                    log.error("Failed to cancel timeout order: {}", order.getId(), e);
                }
            }
        }
    }
}
