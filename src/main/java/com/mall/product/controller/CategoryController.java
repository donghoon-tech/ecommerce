package com.mall.product.controller;

import com.mall.product.dto.CategoryAttributeResponse;
import com.mall.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/{categoryId}/attributes")
    public List<CategoryAttributeResponse> getAttributes(@PathVariable Long categoryId) {
        return categoryService.getAttributesForCategory(categoryId);
    }
}
