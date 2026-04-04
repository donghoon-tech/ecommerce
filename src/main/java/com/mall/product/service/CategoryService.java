package com.mall.product.service;

import com.mall.product.domain.Category;
import com.mall.product.dto.CategoryAttributeResponse;
import com.mall.product.dto.CategoryTreeResponse;
import com.mall.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public List<CategoryTreeResponse> getWholeTree() {
        List<Category> allCategories = categoryRepository.findAll();
        
        Map<Long, List<Category>> childrenMap = allCategories.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        return allCategories.stream()
                .filter(c -> c.getParent() == null)
                .map(root -> buildTreeResponse(root, childrenMap))
                .collect(Collectors.toList());
    }

    private CategoryTreeResponse buildTreeResponse(Category category, Map<Long, List<Category>> childrenMap) {
        List<CategoryTreeResponse> children = childrenMap.getOrDefault(category.getId(), new ArrayList<>())
                .stream()
                .map(child -> buildTreeResponse(child, childrenMap))
                .collect(Collectors.toList());

        return new CategoryTreeResponse(
                category.getId(),
                category.getName(),
                category.getPath(),
                children
        );
    }

    public List<CategoryAttributeResponse> getAttributesForCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리가 존재하지 않습니다: " + categoryId));

        return category.getAttributes().stream()
                .map(attr -> new CategoryAttributeResponse(
                        attr.getId(),
                        attr.getName(),
                        attr.getAttributeType(),
                        attr.isRequired()
                ))
                .collect(Collectors.toList());
    }

    public List<Category> getCategoriesByPath(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리가 존재하지 않습니다: " + categoryId));
        
        return categoryRepository.findAllByPathStartingWith(category.getPath());
    }
}
