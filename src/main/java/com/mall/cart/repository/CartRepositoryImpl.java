package com.mall.cart.repository;

import com.mall.cart.domain.CartItem;
import com.mall.cart.domain.QCartItem;
import com.mall.product.domain.QProduct;
import com.mall.product.domain.QSku;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.mall.cart.domain.QCartItem.cartItem;
import static com.mall.product.domain.QProduct.product;
import static com.mall.product.domain.QSku.sku;

@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CartItem> findByUserIdWithDetails(Long userId) {
        return queryFactory
                .selectFrom(cartItem)
                .join(sku).on(cartItem.skuId.eq(sku.id))
                .join(sku.product, product)
                .where(cartItem.userId.eq(userId))
                .fetch();
    }
}
