package com.mall.product.service;

import com.mall.config.lock.DistributedLock;
import com.mall.product.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final RedissonClient redissonClient;

    private static final String SOLD_OUT_PREFIX = "SOLD_OUT:";

    /**
     * 재고 차감 (3중 방어: 품절 캐시 + 분산 락 + DB Atomic Update)
     */
    @DistributedLock(key = "#skuId")
    public void decreaseStock(Long skuId, int quantity) {
        // 1. Sold-out Cache 확인 (Fail-fast)
        if (isSoldOut(skuId)) {
            throw new IllegalStateException("품절된 상품입니다. SKU: " + skuId);
        }

        // 2. DB Atomic Update 실행
        int updatedRows = inventoryRepository.decreaseStock(skuId, quantity);

        // 3. 업데이트 결과 확인
        if (updatedRows == 0) {
            // 품절 확정 시 캐싱 (10초간 DB 접근 차단)
            markAsSoldOut(skuId);
            throw new IllegalStateException("재고가 부족합니다. SKU: " + skuId);
        }
        
        log.info("Stock decreased successfully. SKU: {}, Qty: -{}", skuId, quantity);
    }

    private boolean isSoldOut(Long skuId) {
        RBucket<String> bucket = redissonClient.getBucket(SOLD_OUT_PREFIX + skuId);
        return bucket.isExists();
    }

    private void markAsSoldOut(Long skuId) {
        RBucket<String> bucket = redissonClient.getBucket(SOLD_OUT_PREFIX + skuId);
        bucket.set("TRUE", 10, TimeUnit.SECONDS);
    }
}
