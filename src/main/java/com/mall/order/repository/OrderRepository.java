package com.mall.order.repository;

import com.mall.order.domain.Order;
import com.mall.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatusAndOrderDateBefore(OrderStatus status, LocalDateTime dateTime);
}
