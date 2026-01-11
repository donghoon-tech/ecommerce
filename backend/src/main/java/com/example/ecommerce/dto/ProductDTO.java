package com.example.ecommerce.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class ProductDTO {
    private UUID id;
    private String slug;
    private String title;
    private BigDecimal price;
    private String category;
    private List<String> imageUrls;
    private String thumbnailUrl;
}
