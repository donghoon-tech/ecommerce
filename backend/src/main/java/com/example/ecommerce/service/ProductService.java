package com.example.ecommerce.service;

import com.example.ecommerce.dto.ProductDTO;
import com.example.ecommerce.entity.BusinessProfile;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.ProductImage;
import com.example.ecommerce.repository.BusinessProfileRepository;
import com.example.ecommerce.repository.ProductImageRepository;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
        private final ProductRepository productRepository;
        private final ProductImageRepository productImageRepository;
        private final BusinessProfileRepository businessProfileRepository;
        private final com.example.ecommerce.mapper.ProductMapper productMapper;

        public List<ProductDTO> getAllProducts() {
                return productRepository.findAll().stream()
                                .filter(Product::isDisplayed) // 전시 가능 상품만
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        // 필요하다면 검색 로직 추가 (Category ID, etc)
        public List<ProductDTO> getProductsBySeller(UUID sellerId) {
                return productRepository.findBySellerId(sellerId).stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        public List<ProductDTO> searchProducts(UUID categoryId, String itemCondition, String itemName) {
                // 복잡한 동적 쿼리는 QueryDSL이 좋지만, 일단 Stream으로 간단 구현
                // 데이터가 많아지면 성능 문제 발생 가능 -> 추후 Repository @Query로 변경 권장
                return productRepository.findAll().stream()
                                .filter(Product::isDisplayed) // 전시 중인 것만
                                .filter(p -> categoryId == null || (p.getCategory() != null
                                                && p.getCategory().getId().equals(categoryId)))
                                .filter(p -> itemCondition == null || p.getItemCondition().equals(itemCondition))
                                .filter(p -> itemName == null || p.getItemName().contains(itemName))
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        public ProductDTO getProductById(UUID id) {
                Product product = productRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Product not found"));
                return convertToDTO(product);
        }

        private ProductDTO convertToDTO(Product product) {
                List<String> imageUrls = productImageRepository.findByProductId(product.getId()).stream()
                                .sorted(Comparator.comparingInt(ProductImage::getDisplayOrder))
                                .map(ProductImage::getImageUrl)
                                .collect(Collectors.toList());

                // 판매자 상호명 조회 (Main Profile)
                String sellerName = businessProfileRepository.findByUserId(product.getSeller().getId()).stream()
                                .filter(BusinessProfile::isMain)
                                .findFirst()
                                .map(BusinessProfile::getBusinessName)
                                .orElse(product.getSeller().getName()); // 없으면 유저 이름

                return productMapper.toDTO(product, sellerName, imageUrls);
        }
}
