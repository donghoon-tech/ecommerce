package com.mall.product.repository;

import com.mall.product.domain.Category;
import com.mall.product.domain.Product;
import com.mall.product.domain.ProductStatus;
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
    private Category tv;

    @BeforeEach
    void setUp() {
        electronics = new Category("Electronics", null);
        em.persist(electronics);
        em.flush();
        em.refresh(electronics);

        tv = new Category("TV", electronics);
        em.persist(tv);
        em.flush();
        em.refresh(tv);

        // 상품 생성 (TV 카테고리, ACTIVE)
        Product oledTv = Product.builder()
                .name("LG OLED TV")
                .basePrice(new BigDecimal("2000000"))
                .category(tv)
                .status(ProductStatus.ACTIVE)
                .attributes(Map.of("display", "OLED", "inch", 65))
                .build();
        productRepository.save(oledTv);

        // 상품 생성 (전자제품 상위 카테고리, ACTIVE)
        Product laptop = Product.builder()
                .name("MacBook Pro")
                .basePrice(new BigDecimal("3000000"))
                .category(electronics)
                .status(ProductStatus.ACTIVE)
                .attributes(Map.of("cpu", "M3", "ram", "16GB"))
                .build();
        productRepository.save(laptop);
        
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("상위 카테고리로 검색하면 하위 카테고리의 상품까지 모두 포함되어야 한다")
    void searchByCategoryHierarchy() {
        ProductSearchRequest request = new ProductSearchRequest(null, electronics.getId(), null, null, 0, 10);
        Page<ProductResponse> result = productRepository.searchProducts(request, PageRequest.of(0, 10));
        
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting("name")
                .containsExactlyInAnyOrder("LG OLED TV", "MacBook Pro");
    }

    @Test
    @DisplayName("상태를 지정하지 않으면 ACTIVE 상품만 검색되어야 한다")
    void searchByActiveStatusByDefault() {
        // Given: DRAFT 상태의 상품 추가
        Product draftProduct = Product.builder()
                .name("Draft Product")
                .basePrice(new BigDecimal("100000"))
                .status(ProductStatus.DRAFT)
                .build();
        productRepository.save(draftProduct);
        em.flush();
        em.clear();

        // When: 상태 지정 없이 검색
        ProductSearchRequest request = new ProductSearchRequest(null, null, null, null, 0, 10);
        Page<ProductResponse> result = productRepository.searchProducts(request, PageRequest.of(0, 10));

        // Then: ACTIVE 상태인 기존 2개 상품만 검색되어야 함 (Draft Product 제외)
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting("name")
                .doesNotContain("Draft Product");
    }

    @Test
    @DisplayName("가격 범위로 필터링이 정확하게 동작해야 한다")
    void searchByPriceRange() {
        ProductSearchRequest request = new ProductSearchRequest(null, null, new BigDecimal("2500000"), null, 0, 10);
        Page<ProductResponse> result = productRepository.searchProducts(request, PageRequest.of(0, 10));
        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("MacBook Pro");
    }
}
