package com.example.ecommerce.mapper;

import com.example.ecommerce.dto.ProductDTO;
import com.example.ecommerce.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", source = "product.id")
    @Mapping(target = "categoryId", source = "product.category.id")
    @Mapping(target = "categoryName", source = "product.category.name")

    // Seller Info
    @Mapping(target = "sellerId", source = "product.seller.id")
    @Mapping(target = "sellerName", source = "sellerName")

    // Product Info
    @Mapping(target = "itemName", source = "product.itemName")
    @Mapping(target = "itemCondition", source = "product.itemCondition")
    @Mapping(target = "unitPrice", source = "product.unitPrice")
    @Mapping(target = "saleUnit", source = "product.saleUnit")
    @Mapping(target = "stockQuantity", source = "product.stockQuantity")
    @Mapping(target = "totalAmount", source = "product.totalAmount")

    // Address & Status
    @Mapping(target = "loadingAddress", source = "product.loadingAddress")
    @Mapping(target = "loadingAddressDisplay", source = "product.loadingAddressDisplay")
    @Mapping(target = "status", source = "product.status")
    @Mapping(target = "rejectionReason", source = "product.rejectionReason")
    @Mapping(target = "createdAt", source = "product.createdAt")

    // Images
    @Mapping(target = "imageUrls", source = "imageUrls")
    ProductDTO toDTO(Product product, String sellerName, List<String> imageUrls);
}
