package com.mall.product.service;

import com.mall.product.domain.Category;
import com.mall.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Category createCategory(String name, Long parentId) {
        Category parent = null;
        if (parentId != null) {
            parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("부모 카테고리가 존재하지 않습니다: " + parentId));
        }

        Category category = new Category(name, parent);
        return categoryRepository.save(category);
    }

    public List<Category> getCategoriesByPath(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리가 존재하지 않습니다: " + categoryId));
        
        return categoryRepository.findAllByPathStartingWith(category.getPath());
    }

    public List<Category> getAllRootCategories() {
        return categoryRepository.findAllByPathStartingWith("");
    }
}
