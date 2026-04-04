package com.mall.product.repository;

import com.mall.product.domain.*;
import com.mall.product.dto.ProductResponse;
import com.mall.product.dto.ProductSearchRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;
    
    @Autowired
    private EntityManager em;

    private Category electronics;
    private Category smartphone;

    @BeforeEach
    void setUp() {
        electronics = new Category("Electronics", null);
        em.persist(electronics);
        
        smartphone = new Category("Smartphone", electronics);
        em.persist(smartphone);

        // Metadata: Color and Storage are selling options
        CategoryAttribute colorAttr = CategoryAttribute.builder()
                .name("Color").attributeType("STRING").required(true).isOption(true).build();
        CategoryAttribute storageAttr = CategoryAttribute.builder()
                .name("Storage").attributeType("STRING").required(true).isOption(true).build();
        CategoryAttribute cpuAttr = CategoryAttribute.builder()
                .name("CPU").attributeType("STRING").required(true).isOption(false).build();
        
        smartphone.addAttribute(colorAttr);
        smartphone.addAttribute(storageAttr);
        smartphone.addAttribute(cpuAttr);
        
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("구조화된 SKU 속성(JSONB)을 통해 특정 옵션 조합의 상품을 정확히 찾을 수 있다")
    void createAndFindProductWithStructuredSkus() {
        // Given: iPhone 15 with 2 SKUs
        Category smartphoneCategory = em.find(Category.class, smartphone.getId());
        
        Product iphone = Product.builder()
                .name("iPhone 15")
                .basePrice(new BigDecimal("1200000"))
                .category(smartphoneCategory)
                .status(ProductStatus.ACTIVE)
                .attributes(Map.of("CPU", "A16 Bionic"))
                .build();

        // SKU 1: Black / 128GB
        Sku black128 = Sku.builder()
                .skuCode("IP15-BLK-128")
                .attributes(Map.of("Color", "Black", "Storage", "128GB"))
                .additionalPrice(BigDecimal.ZERO)
                .build();
        iphone.addSku(black128);

        // SKU 2: White / 256GB
        Sku white256 = Sku.builder()
                .skuCode("IP15-WHT-256")
                .attributes(Map.of("Color", "White", "Storage", "256GB"))
                .additionalPrice(new BigDecimal("200000"))
                .build();
        iphone.addSku(white256);

        productRepository.save(iphone);

        // Inventory 저장 (Builder 사용)
        inventoryRepository.save(Inventory.builder()
                .stockQuantity(10)
                .sku(black128)
                .build());
        inventoryRepository.save(Inventory.builder()
                .stockQuantity(5)
                .sku(white256)
                .build());

        em.flush();
        em.clear();

        // When: Search
        ProductSearchRequest request = new ProductSearchRequest(null, smartphone.getId(), null, null, 0, 10);
        Page<ProductResponse> result = productRepository.searchProducts(request, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        ProductResponse foundProduct = result.getContent().get(0);
        assertThat(foundProduct.skus()).hasSize(2);
        
        var blackSku = foundProduct.skus().stream().filter(s -> s.skuCode().equals("IP15-BLK-128")).findFirst().get();
        assertThat(blackSku.stockQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("상위 카테고리 검색 시 하위 카테고리 상품이 모두 포함된다")
    void searchByCategoryHierarchy() {
        // Given
        Category smartphoneCategory = em.find(Category.class, smartphone.getId());
        Product p = Product.builder()
                .name("Test Phone")
                .basePrice(new BigDecimal("500000"))
                .category(smartphoneCategory)
                .status(ProductStatus.ACTIVE)
                .build();
        productRepository.save(p);
        em.flush();
        em.clear();

        // When
        ProductSearchRequest request = new ProductSearchRequest(null, electronics.getId(), null, null, 0, 10);
        Page<ProductResponse> result = productRepository.searchProducts(request, PageRequest.of(0, 10));
        
        // Then
        assertThat(result.getContent()).hasSize(1);
    }
}
