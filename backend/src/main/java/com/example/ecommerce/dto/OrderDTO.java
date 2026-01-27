package com.example.ecommerce.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class OrderDTO {
    private UUID id;

    private UserDTO buyer;
    private UserDTO seller;

    private String orderType; // platform, phone

    private String truckTonnage;
    private String truckType;

    private String shippingLoadingAddress;
    private String shippingUnloadingAddress;

    private String recipientName;
    private String recipientPhone;

    private BigDecimal totalAmount;
    private String status;
    private String paymentStatus;

    private String orderMemo;
    private String adminMemo;

    private LocalDateTime deliveryStartedAt;
    private LocalDateTime deliveryCompletedAt;
    private String carrierInfo;

    private LocalDateTime createdAt;

    // 주문 상품 목록 (OrderItemDTO가 필요하지만 일단 간단히 처리 혹은 추가 필요)
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        private String productNameSnapshot;
        private String productConditionSnapshot;
        private BigDecimal priceSnapshot;
        private int quantity;
        private BigDecimal subtotal;
    }
}