package com.mall.cart.service;

import com.mall.cart.domain.CartItem;
import com.mall.cart.dto.CartResponse;
import com.mall.cart.repository.CartRepository;
import com.mall.product.domain.Product;
import com.mall.product.domain.Sku;
import com.mall.product.repository.SkuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final SkuRepository skuRepository;
    private final RedissonClient redissonClient;

    private static final String GUEST_CART_PREFIX = "temp_cart:";
    private static final String USER_CART_CACHE_PREFIX = "cart:user:";

    @Override
    @Transactional
    public void addItem(Long userId, String guestToken, Long skuId, int quantity) {
        if (userId != null) {
            CartItem cartItem = cartRepository.findByUserIdAndSkuId(userId, skuId)
                    .orElseGet(() -> CartItem.builder()
                            .userId(userId)
                            .skuId(skuId)
                            .quantity(0)
                            .build());
            cartItem.addQuantity(quantity);
            cartRepository.save(cartItem);
            evictUserCartCache(userId);
        } else {
            RMap<Long, Integer> guestCart = getGuestCart(guestToken);
            guestCart.merge(skuId, quantity, Integer::sum);
            guestCart.expire(24, TimeUnit.HOURS);
        }
    }

    @Override
    @Transactional
    public void updateQuantity(Long userId, String guestToken, Long skuId, int quantity) {
        if (userId != null) {
            cartRepository.findByUserIdAndSkuId(userId, skuId)
                    .ifPresent(item -> {
                        item.updateQuantity(quantity);
                        cartRepository.save(item);
                        evictUserCartCache(userId);
                    });
        } else {
            RMap<Long, Integer> guestCart = getGuestCart(guestToken);
            if (quantity > 0) {
                guestCart.put(skuId, quantity);
            } else {
                guestCart.remove(skuId);
            }
        }
    }

    @Override
    @Transactional
    public void removeItem(Long userId, String guestToken, Long skuId) {
        if (userId != null) {
            cartRepository.findByUserIdAndSkuId(userId, skuId)
                    .ifPresent(item -> {
                        cartRepository.delete(item);
                        evictUserCartCache(userId);
                    });
        } else {
            getGuestCart(guestToken).remove(skuId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartResponse> getCartItems(Long userId, String guestToken) {
        if (userId != null) {
            // [PERF-DEBT: PHASE-4-HYBRID] - Later we can check Redis cache first
            return cartRepository.findByUserIdWithDetails(userId).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } else {
            RMap<Long, Integer> guestCart = getGuestCart(guestToken);
            return guestCart.entrySet().stream()
                    .map(entry -> {
                        Sku sku = skuRepository.findById(entry.getKey())
                                .orElseThrow(() -> new IllegalArgumentException("Invalid Sku ID: " + entry.getKey()));
                        return mapToResponse(sku, entry.getValue());
                    })
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public void mergeGuestCart(Long userId, String guestToken) {
        if (guestToken == null) return;

        RMap<Long, Integer> guestCart = getGuestCart(guestToken);
        if (guestCart.isEmpty()) return;

        log.info("Merging guest cart for user: {}, token: {}", userId, guestToken);
        guestCart.forEach((skuId, quantity) -> addItem(userId, null, skuId, quantity));
        
        guestCart.clear();
        evictUserCartCache(userId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
        evictUserCartCache(userId);
    }

    private RMap<Long, Integer> getGuestCart(String guestToken) {
        return redissonClient.getMap(GUEST_CART_PREFIX + guestToken);
    }

    private void evictUserCartCache(Long userId) {
        redissonClient.getBucket(USER_CART_CACHE_PREFIX + userId).delete();
    }

    private CartResponse mapToResponse(CartItem item) {
        Sku sku = skuRepository.findById(item.getSkuId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Sku ID"));
        return mapToResponse(sku, item.getQuantity());
    }

    private CartResponse mapToResponse(Sku sku, int quantity) {
        Product product = sku.getProduct();
        BigDecimal price = product.getBasePrice().add(sku.getAdditionalPrice());
        return new CartResponse(
                sku.getId(),
                product.getName(),
                sku.getSkuCode(),
                sku.getAttributes(),
                price,
                quantity,
                price.multiply(BigDecimal.valueOf(quantity))
        );
    }
}
