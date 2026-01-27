package com.example.ecommerce.mapper;

import com.example.ecommerce.dto.OrderDTO;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface OrderMapper {

    // Order 엔티티에 orderItems 리스트가 없으므로 일단 매핑 제외.
    // 필요 시 @Context 등을 통해 별도로 주입하거나, Order 엔티티에 양방향 매핑 추가 필요.
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "buyer", source = "buyer")
    @Mapping(target = "seller", source = "seller")
    @Mapping(target = "orderType", source = "orderType")
    @Mapping(target = "truckType", source = "truckType")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "paymentStatus", source = "paymentStatus")
    OrderDTO toDTO(Order order);

    // Entity 필드명과 DTO 필드명이 같으므로 별도 Mapping 불필요
    // productNameSnapshot -> productNameSnapshot
    OrderDTO.OrderItemDTO toItemDTO(OrderItem item);
}
