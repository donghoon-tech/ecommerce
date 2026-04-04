package com.mall.product.service;

import com.mall.product.domain.*;
import com.mall.product.dto.ProductCreateRequest;
import com.mall.product.dto.ProductResponse;
import com.mall.product.dto.ProductSearchRequest;
import com.mall.product.dto.SkuResponse;
import com.mall.product.repository.CategoryRepository;
import com.mall.product.repository.InventoryRepository;
import com.mall.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;

    public Page<ProductResponse> searchProducts(ProductSearchRequest request) {
        Pageable pageable = PageRequest.of(request.page(), request.size());
        return productRepository.searchProducts(request, pageable);
    }

    @Transactional
    public Long createProduct(ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리가 존재하지 않습니다: " + request.categoryId()));

        Product product = Product.builder()
                .name(request.name())
                .basePrice(request.basePrice())
                .category(category)
                .attributes(request.attributes())
                .status(ProductStatus.DRAFT)
                .build();

        if (request.skus() != null) {
            request.skus().forEach(skuReq -> {
                Sku sku = Sku.builder()
                        .skuCode(skuReq.skuCode())
                        .attributes(skuReq.attributes())
                        .additionalPrice(skuReq.additionalPrice())
                        .build();
                product.addSku(sku);
                
                // Inventory 생성 및 연결
                Inventory inventory = Inventory.builder()
                        .sku(sku)
                        .stockQuantity(skuReq.stockQuantity())
                        .build();
                inventoryRepository.save(inventory);
            });
        }

        return productRepository.save(product).getId();
    }

    @Transactional
    public void updateStatus(Long productId, ProductStatus status) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다: " + productId));
        product.updateStatus(status);
    }
    
    public ProductResponse getProduct(Long productId) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다: " + productId));
                
        return mapToProductResponse(p);
    }

    public SkuResponse getSkuByOptions(Long productId, Map<String, Object> options) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다: " + productId));

        Sku sku = product.getSkus().stream()
                .filter(s -> s.getAttributes().equals(options))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 옵션 조합의 상품이 존재하지 않습니다."));

        return mapToSkuResponse(sku);
    }

    private ProductResponse mapToProductResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getBasePrice(),
                p.getAttributes(),
                p.getSkus().stream()
                        .map(this::mapToSkuResponse)
                        .collect(Collectors.toList())
        );
    }

    private SkuResponse mapToSkuResponse(Sku s) {
        Integer stock = inventoryRepository.findBySkuId(s.getId())
                .map(Inventory::getStockQuantity)
                .orElse(0);

        return new SkuResponse(
                s.getId(),
                s.getSkuCode(),
                s.getAttributes(),
                s.getAdditionalPrice(),
                stock
        );
    }
}
