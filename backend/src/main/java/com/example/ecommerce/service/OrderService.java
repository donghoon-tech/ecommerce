package com.example.ecommerce.service;

import com.example.ecommerce.dto.OrderDTO;
import com.example.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final com.example.ecommerce.mapper.OrderMapper orderMapper;

    public List<OrderDTO> getMyOrders(UUID userId) {
        return orderRepository.findByBuyerId(userId).stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    // 필요 시 판매자 주문 조회, 상세 조회 추가
}