package com.example.ecommerce.service;

import com.example.ecommerce.dto.ProductDTO;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.mapper.ProductMapper;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        // N+1 문제 방지를 위해 카테고리를 한 번에 로딩하는 것이 좋지만, 
        // 지금은 데이터가 적으므로 단순하게 구현하거나, 캐싱하거나, Map으로 변환해서 매핑합니다.
        
        List<Category> categories = categoryRepository.findAll();
        Map<UUID, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        return products.stream()
                .map(product -> productMapper.toDTO(product, categoryMap.get(product.getCategoryId())))
                .collect(Collectors.toList());
    }
    
    // 검색 필터
    public List<ProductDTO> searchProducts(UUID categoryId, String grade, String itemName) {
        List<Product> products = productRepository.searchProducts(categoryId, grade, itemName);
        
        // 카테고리 이름 매핑 (최적화 여지 있음)
        List<Category> categories = categoryRepository.findAll();
        Map<UUID, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
                
        return products.stream()
                .map(product -> productMapper.toDTO(product, categoryMap.get(product.getCategoryId())))
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        String categoryName = categoryRepository.findById(product.getCategoryId())
                .map(Category::getName)
                .orElse("Unknown");
                
        return productMapper.toDTO(product, categoryName);
    }
}