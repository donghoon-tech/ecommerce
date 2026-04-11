package com.mall.cart.repository;

import com.mall.cart.domain.CartItem;
import java.util.List;

public interface CartRepositoryCustom {
    List<CartItem> findByUserIdWithDetails(Long userId);
}
