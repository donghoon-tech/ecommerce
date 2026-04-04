package com.mall.product.repository;

import com.mall.product.domain.ProductStatus;
import com.mall.product.domain.QCategory;
import com.mall.product.domain.QProduct;
import com.mall.product.domain.QSku;
import com.mall.product.dto.ProductResponse;
import com.mall.product.dto.ProductSearchRequest;
import com.mall.product.dto.SkuResponse;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.mall.product.domain.QProduct.product;
import static com.mall.product.domain.QCategory.category;
import static com.mall.product.domain.QSku.sku;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductResponse> searchProducts(ProductSearchRequest request, Pageable pageable) {
        List<Long> productIds = queryFactory
                .select(product.id)
                .from(product)
                .leftJoin(product.category, category)
                .where(
                        nameContains(request.name()),
                        categoryPathStartsWith(request.categoryId()),
                        priceBetween(request.minPrice(), request.maxPrice()),
                        statusEq(request.status())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(product.count())
                .from(product)
                .leftJoin(product.category, category)
                .where(
                        nameContains(request.name()),
                        categoryPathStartsWith(request.categoryId()),
                        priceBetween(request.minPrice(), request.maxPrice()),
                        statusEq(request.status())
                )
                .fetchOne();

        List<ProductResponse> content = queryFactory
                .selectFrom(product)
                .leftJoin(product.skus, sku).fetchJoin()
                .where(product.id.in(productIds))
                .fetch()
                .stream()
                .map(p -> new ProductResponse(
                        p.getId(),
                        p.getName(),
                        p.getBasePrice(),
                        p.getAttributes(),
                        p.getSkus().stream()
                                .map(s -> new SkuResponse(
                                        s.getId(),
                                        s.getSkuCode(),
                                        s.getAttributes(),
                                        s.getAdditionalPrice(),
                                        s.getStockQuantity()
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? product.name.contains(name) : null;
    }

    private BooleanExpression categoryPathStartsWith(Long categoryId) {
        if (categoryId == null) return null;
        
        String path = queryFactory
                .select(category.path)
                .from(category)
                .where(category.id.eq(categoryId))
                .fetchOne();
                
        return path != null ? category.path.startsWith(path) : null;
    }

    private BooleanExpression priceBetween(BigDecimal min, BigDecimal max) {
        if (min == null && max == null) return null;
        if (min != null && max != null) return product.basePrice.between(min, max);
        if (min != null) return product.basePrice.goe(min);
        return product.basePrice.loe(max);
    }

    private BooleanExpression statusEq(ProductStatus status) {
        return status != null ? product.status.eq(status) : product.status.eq(ProductStatus.ACTIVE);
    }
}
