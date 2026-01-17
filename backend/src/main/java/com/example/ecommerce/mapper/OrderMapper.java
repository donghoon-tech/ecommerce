package com.example.ecommerce.mapper;

import com.example.ecommerce.dto.OrderDTO;
import com.example.ecommerce.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { ProductMapper.class, MemberMapper.class })
public interface OrderMapper {
    OrderDTO toDTO(Order order);

    Order toEntity(OrderDTO dto);

    List<OrderDTO> toDTOs(List<Order> orders);
}
