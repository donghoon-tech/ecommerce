package com.mall.product.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CategoryAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 속성 명 (예: 해상도, 소재)

    @Column(nullable = false)
    private String attributeType; // STRING, NUMBER, BOOLEAN 등

    private boolean required; // 필수 여부

    private boolean isOption; // 판매 옵션 여부 (true면 SKU 속성, false면 Product 공통 속성)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    public void assignCategory(Category category) {
        this.category = category;
    }
}
