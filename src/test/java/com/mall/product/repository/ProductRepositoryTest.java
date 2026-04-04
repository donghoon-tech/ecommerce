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
                .attributes(Map.of("CPU", "A16 Bionic")) // Common Spec
                .build();

        // SKU 1: Black / 128GB
        Sku black128 = Sku.builder()
                .skuCode("IP15-BLK-128")
                .attributes(Map.of("Color", "Black", "Storage", "128GB"))
                .additionalPrice(BigDecimal.ZERO)
                .stockQuantity(10)
                .build();
        iphone.addSku(black128);

        // SKU 2: White / 256GB
        Sku white256 = Sku.builder()
                .skuCode("IP15-WHT-256")
                .attributes(Map.of("Color", "White", "Storage", "256GB"))
                .additionalPrice(new BigDecimal("200000"))
                .stockQuantity(5)
                .build();
        iphone.addSku(white256);

        productRepository.save(iphone);
        em.flush();
        em.clear();

        // When: Search for ACTIVE products in Smartphone category
        ProductSearchRequest request = new ProductSearchRequest(null, smartphone.getId(), null, null, 0, 10);
        Page<ProductResponse> result = productRepository.searchProducts(request, PageRequest.of(0, 10));

        // Then: Verify structured data
        assertThat(result.getContent()).hasSize(1);
        ProductResponse foundProduct = result.getContent().get(0);
        assertThat(foundProduct.name()).isEqualTo("iPhone 15");
        assertThat(foundProduct.attributes()).containsEntry("CPU", "A16 Bionic");
        
        // Check SKUs
        assertThat(foundProduct.skus()).hasSize(2);
        assertThat(foundProduct.skus()).extracting("skuCode")
                .containsExactlyInAnyOrder("IP15-BLK-128", "IP15-WHT-256");
        
        // Verify specific SKU attributes (JSONB)
        var blackSku = foundProduct.skus().stream().filter(s -> s.skuCode().equals("IP15-BLK-128")).findFirst().get();
        assertThat(blackSku.attributes()).containsEntry("Color", "Black");
        assertThat(blackSku.attributes()).containsEntry("Storage", "128GB");
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

        // When: Search by Electronics (Root)
        ProductSearchRequest request = new ProductSearchRequest(null, electronics.getId(), null, null, 0, 10);
        Page<ProductResponse> result = productRepository.searchProducts(request, PageRequest.of(0, 10));
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Test Phone");
    }
}
