package com.mall.product.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column(nullable = false)
    private String path;

    public Category(String name, Category parent) {
        this.name = name;
        this.parent = parent;
        this.path = generatePath(parent);
    }

    // Test constructor
    protected Category(Long id, String name, Category parent) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        this.path = generatePath(parent);
    }

    private String generatePath(Category parent) {
        if (parent == null) {
            return (id != null ? id : "") + "/";
        }
        return parent.getPath() + (id != null ? id : "") + "/";
    }
}
