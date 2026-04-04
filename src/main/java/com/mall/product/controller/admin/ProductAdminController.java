package com.mall.product.controller.admin;

import com.mall.product.dto.CategoryTreeResponse;
import com.mall.product.dto.ProductResponse;
import com.mall.product.dto.ProductSearchRequest;
import com.mall.product.service.CategoryService;
import com.mall.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ProductAdminController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public String index() {
        return "admin/index";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        List<CategoryTreeResponse> tree = categoryService.getWholeTree();
        model.addAttribute("categoryTree", tree);
        return "admin/categories";
    }

    @GetMapping("/products")
    public String products(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        ProductSearchRequest request = new ProductSearchRequest(name, categoryId, null, null, page, size);
        Page<ProductResponse> products = productService.searchProducts(request);
        
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getWholeTree()); // For filter
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("categories", categoryService.getWholeTree());
        return "admin/product-form";
    }
}
