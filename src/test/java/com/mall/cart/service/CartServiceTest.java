package com.mall.cart.service;

import com.mall.cart.dto.CartResponse;
import com.mall.cart.repository.CartRepository;
import com.mall.product.domain.Product;
import com.mall.product.domain.Sku;
import com.mall.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private RedissonClient redissonClient;

    private Sku testSku;

    @BeforeEach
    void setUp() {
        Product product = Product.builder()
                .name("Cart Test Product")
                .basePrice(new BigDecimal("50000"))
                .attributes(Map.of())
                .build();
        
        testSku = Sku.builder()
                .skuCode("CART-TEST-001")
                .attributes(Map.of())
                .additionalPrice(BigDecimal.ZERO)
                .build();
        product.addSku(testSku);
        productRepository.save(product);
    }

    @Test
    @DisplayName("비회원이 담은 상품을 로그인 시 정회원 장바구니로 병합할 수 있다")
    void mergeGuestCartTest() {
        // Given
        String guestToken = UUID.randomUUID().toString();
        Long userId = 100L;

        // 비회원 장바구니에 상품 추가
        cartService.addItem(null, guestToken, testSku.getId(), 2);

        // When
        cartService.mergeGuestCart(userId, guestToken);

        // Then
        List<CartResponse> userCart = cartService.getCartItems(userId, null);
        assertThat(userCart).hasSize(1);
        assertThat(userCart.get(0).quantity()).isEqualTo(2);
        assertThat(userCart.get(0).skuId()).isEqualTo(testSku.getId());

        // Redis 비회원 장바구니가 비어있는지 확인
        assertThat(redissonClient.getMap("temp_cart:" + guestToken).isEmpty()).isTrue();
    }
}
