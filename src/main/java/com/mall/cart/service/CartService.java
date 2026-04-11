package com.mall.cart.service;

import com.mall.cart.dto.CartResponse;
import java.util.List;

public interface CartService {
    void addItem(Long userId, String guestToken, Long skuId, int quantity);
    void updateQuantity(Long userId, String guestToken, Long skuId, int quantity);
    void removeItem(Long userId, String guestToken, Long skuId);
    List<CartResponse> getCartItems(Long userId, String guestToken);
    void mergeGuestCart(Long userId, String guestToken);
    void clearCart(Long userId);
}
