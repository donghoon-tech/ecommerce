package com.example.ecommerce.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class OrderDTO {
    private UUID id;
    private String orderNumber;
    private ProductDTO product;
    private MemberDTO seller;
    private MemberDTO buyer;
    private String status;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String deliveryAddress;
    private String notes;
    private LocalDateTime createdAt;
}
