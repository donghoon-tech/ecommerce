package com.mall.product.controller;

import com.mall.product.dto.ProductResponse;
import com.mall.product.dto.ProductSearchRequest;
import com.mall.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(ProductSearchRequest request) {
        return ResponseEntity.ok(productService.searchProducts(request));
    }
}
