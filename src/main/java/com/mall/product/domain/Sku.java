package com.mall.product.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Sku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String skuCode; // 재고 관리 식별자

    @Column(nullable = false)
    private BigDecimal additionalPrice; // 추가 금액

    @Column(nullable = false)
    private Integer stockQuantity; // 재고 수량

    /**
     * PostgreSQL JSONB 활용: SKU별 판매 옵션 저장 (예: 색상, 사이즈, 규격)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> attributes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    public void assignProduct(Product product) {
        this.product = product;
    }

    /**
     * 재고 차감 (동시성 고려는 서비스 레벨에서)
     */
    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다. SKU: " + skuCode);
        }
        this.stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }
}
