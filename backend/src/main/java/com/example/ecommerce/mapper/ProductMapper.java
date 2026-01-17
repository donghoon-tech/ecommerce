package com.example.ecommerce.mapper;

import com.example.ecommerce.dto.ProductDTO;
import com.example.ecommerce.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductDTO toDTO(Product product, String categoryName) {
        if (product == null) return null;
        
        return ProductDTO.builder()
                .id(product.getId())
                .categoryId(product.getCategoryId())
                .categoryName(categoryName)
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .grade(product.getGrade())
                .itemName(product.getItemName())
                .spec(product.getSpec())
                .imageUrls(product.getImageUrls())
                .thumbnailUrl(product.getThumbnailUrl())
                .description(product.getDescription())
                .build();
    }
    
    // categoryName 없는 버전 (기본)
    public ProductDTO toDTO(Product product) {
        return toDTO(product, null);
    }

    public Product toEntity(ProductDTO dto) {
        if (dto == null) return null;

        return Product.builder()
                .id(dto.getId())
                .categoryId(dto.getCategoryId())
                .name(dto.getName())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .grade(dto.getGrade())
                .itemName(dto.getItemName())
                .spec(dto.getSpec())
                .imageUrls(dto.getImageUrls())
                .thumbnailUrl(dto.getThumbnailUrl())
                .description(dto.getDescription())
                .build();
    }
}
