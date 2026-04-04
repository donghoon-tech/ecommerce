package com.mall.product.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer stockQuantity;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", unique = true)
    private Sku sku;

    public void updateStock(int quantity) {
        this.stockQuantity = quantity;
    }
}
