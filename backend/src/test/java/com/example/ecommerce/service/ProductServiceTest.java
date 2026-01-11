package com.example.ecommerce.service;

import com.example.ecommerce.dto.ProductDTO;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.mapper.ProductMapper;
import com.example.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void getAllProducts() {
        // Given
        Product product = new Product();
        product.setTitle("Test Product");
        product.setPrice(BigDecimal.TEN);

        ProductDTO productDTO = new ProductDTO();
        productDTO.setTitle("Test Product");
        productDTO.setPrice(BigDecimal.TEN);

        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productMapper.toDTOs(List.of(product))).thenReturn(List.of(productDTO));

        // When
        List<ProductDTO> result = productService.getAllProducts();

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getTitle());
    }
}
