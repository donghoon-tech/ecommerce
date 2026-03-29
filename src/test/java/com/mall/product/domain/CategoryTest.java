package com.mall.product.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    @Test
    @DisplayName("Root 카테고리를 생성하면 path는 자신의 ID와 구분자로 구성된다")
    void createRootCategory() {
        Category root = new Category(1L, "전자제품", null);
        assertThat(root.getPath()).isEqualTo("1/");
    }

    @Test
    @DisplayName("하위 카테고리를 생성하면 부모의 path에 자신의 ID가 추가된다")
    void createChildCategory() {
        Category root = new Category(1L, "전자제품", null);
        Category child = new Category(5L, "가전", root);
        
        assertThat(child.getPath()).isEqualTo("1/5/");
    }
}
