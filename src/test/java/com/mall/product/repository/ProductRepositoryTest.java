package com.mall.product.repository;

import com.mall.product.domain.Category;
import com.mall.product.domain.Product;
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
        // [Task 6: Step 1] Setup category path hierarchy
        electronics = new Category("Electronics", null);
        em.persist(electronics);
        
        // Materialized Path 갱신 (ID가 영속화된 후 수동 반영 - 실제 환경에선 리스너 등으로 처리)
        em.flush();
        em.refresh(electronics); // path 반영

        tv = new Category("TV", electronics);
        em.persist(tv);
        em.flush();
        em.refresh(tv);

        // 상품 생성 (TV 카테고리)
        Product oledTv = Product.builder()
                .name("LG OLED TV")
                .basePrice(new BigDecimal("2000000"))
                .category(tv)
                .attributes(Map.of("display", "OLED", "inch", 65))
                .build();
        productRepository.save(oledTv);

        // 상품 생성 (전자제품 상위 카테고리)
        Product laptop = Product.builder()
                .name("MacBook Pro")
                .basePrice(new BigDecimal("3000000"))
                .category(electronics)
                .attributes(Map.of("cpu", "M3", "ram", "16GB"))
                .build();
        productRepository.save(laptop);
        
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("상위 카테고리로 검색하면 하위 카테고리의 상품까지 모두 포함되어야 한다")
    void searchByCategoryHierarchy() {
        // Given: Electronics 카테고리 ID로 검색 요청
        ProductSearchRequest request = new ProductSearchRequest(null, electronics.getId(), null, null, 0, 10);
        
        // When
        Page<ProductResponse> result = productRepository.searchProducts(request, PageRequest.of(0, 10));
        
        // Then: LG OLED TV(하위)와 MacBook Pro(본인) 모두 검색되어야 함
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting("name")
                .containsExactlyInAnyOrder("LG OLED TV", "MacBook Pro");
    }

    @Test
    @DisplayName("가격 범위로 필터링이 정확하게 동작해야 한다")
    void searchByPriceRange() {
        // Given: 2,500,000원 이상 상품 검색
        ProductSearchRequest request = new ProductSearchRequest(null, null, new BigDecimal("2500000"), null, 0, 10);
        
        // When
        Page<ProductResponse> result = productRepository.searchProducts(request, PageRequest.of(0, 10));
        
        // Then: MacBook Pro만 검색되어야 함
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("MacBook Pro");
    }

    @Test
    @DisplayName("상품 검색 결과에 JSON 가변 속성이 올바르게 포함되어야 한다")
    void searchResultWithJsonAttributes() {
        // Given: MacBook Pro 검색
        ProductSearchRequest request = new ProductSearchRequest("MacBook", null, null, null, 0, 10);
        
        // When
        Page<ProductResponse> result = productRepository.searchProducts(request, PageRequest.of(0, 10));
        
        // Then: JSON 속성 확인
        ProductResponse response = result.getContent().get(0);
        assertThat(response.attributes()).containsEntry("cpu", "M3");
        assertThat(response.attributes()).containsEntry("ram", "16GB");
    }
}
