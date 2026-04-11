package com.mall.cart.repository;

import com.mall.cart.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartItem, Long>, CartRepositoryCustom {
    Optional<CartItem> findByUserIdAndSkuId(Long userId, Long skuId);
    void deleteByUserId(Long userId);
}
