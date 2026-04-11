package com.mall.cart.repository;

import com.mall.cart.domain.CartItem;
import com.mall.product.domain.Product;
import com.mall.product.domain.Sku;
import com.mall.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("장바구니 아이템을 저장하고 사용자 ID로 상세 정보를 포함하여 조회할 수 있다")
    void saveAndFindByUserIdWithDetails() {
        // Given
        Product product = Product.builder()
                .name("Test Product")
                .basePrice(new BigDecimal("10000"))
                .attributes(Map.of())
                .build();
        
        Sku sku = Sku.builder()
                .skuCode("TEST-SKU-001")
                .attributes(Map.of())
                .additionalPrice(BigDecimal.ZERO)
                .build();
        product.addSku(sku);
        productRepository.save(product);

        Long userId = 1L;
        CartItem cartItem = CartItem.builder()
                .userId(userId)
                .skuId(sku.getId())
                .quantity(2)
                .build();
        cartRepository.save(cartItem);

        em.flush();
        em.clear();

        // When
        List<CartItem> results = cartRepository.findByUserIdWithDetails(userId);

        // Then
        assertThat(results).hasSize(1);
        CartItem found = results.get(0);
        assertThat(found.getUserId()).isEqualTo(userId);
        assertThat(found.getSkuId()).isEqualTo(sku.getId());
        assertThat(found.getQuantity()).isEqualTo(2);
    }
}
