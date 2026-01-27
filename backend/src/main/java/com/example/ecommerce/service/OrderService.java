package com.example.ecommerce.service;

import com.example.ecommerce.dto.OrderDTO;
import com.example.ecommerce.dto.UserDTO;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.User;
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

    public List<OrderDTO> getMyOrders(UUID userId) {
        return orderRepository.findByBuyerId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 필요 시 판매자 주문 조회, 상세 조회 추가

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderType(order.getOrderType().name());
        dto.setTruckTonnage(order.getTruckTonnage());
        dto.setTruckType(order.getTruckType() != null ? order.getTruckType().name() : null);
        dto.setShippingLoadingAddress(order.getShippingLoadingAddress());
        dto.setShippingUnloadingAddress(order.getShippingUnloadingAddress());
        dto.setRecipientName(order.getRecipientName());
        dto.setRecipientPhone(order.getRecipientPhone());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setPaymentStatus(order.getPaymentStatus().name());
        dto.setOrderMemo(order.getOrderMemo());
        dto.setAdminMemo(order.getAdminMemo());
        dto.setDeliveryStartedAt(order.getDeliveryStartedAt());
        dto.setDeliveryCompletedAt(order.getDeliveryCompletedAt());
        dto.setCarrierInfo(order.getCarrierInfo());
        dto.setCreatedAt(order.getCreatedAt());

        dto.setBuyer(toUserDTO(order.getBuyer()));
        dto.setSeller(toUserDTO(order.getSeller()));

        return dto;
    }

    private UserDTO toUserDTO(User user) {
        if (user == null)
            return null;
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .representativePhone(user.getRepresentativePhone())
                .email(user.getEmail())
                .role(user.getRole())
                .businessNumber(user.getBusinessNumber())
                .build();
    }
}