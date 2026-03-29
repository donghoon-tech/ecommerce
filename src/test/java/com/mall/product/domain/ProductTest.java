package com.mall.product.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {

    @Test
    @DisplayName("Product를 생성하면 기본 정보와 가변 속성(JSONB)이 올바르게 저장된다")
    void createProduct() {
        Map<String, Object> attributes = Map.of(
            "resolution", "4K",
            "refresh_rate", "120Hz"
        );
        
        Product product = Product.builder()
                .name("OLED TV")
                .basePrice(new BigDecimal("2000000"))
                .attributes(attributes)
                .build();
        
        assertThat(product.getName()).isEqualTo("OLED TV");
        assertThat(product.getBasePrice()).isEqualTo(new BigDecimal("2000000"));
        assertThat(product.getAttributes()).containsAllEntriesOf(attributes);
    }
}
