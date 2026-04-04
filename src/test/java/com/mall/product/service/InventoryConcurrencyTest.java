package com.mall.product.service;

import com.mall.product.domain.Inventory;
import com.mall.product.domain.Product;
import com.mall.product.domain.Sku;
import com.mall.product.repository.InventoryRepository;
import com.mall.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class InventoryConcurrencyTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private Long targetSkuId;

    @BeforeEach
    void setUp() {
        // 테스트용 상품 및 재고(10개) 생성
        Product product = Product.builder()
                .name("Concurrency Test Product")
                .basePrice(new BigDecimal("10000"))
                .attributes(Map.of())
                .build();
        
        Sku sku = Sku.builder()
                .skuCode("CONC-TEST-001")
                .attributes(Map.of())
                .additionalPrice(BigDecimal.ZERO)
                .build();
        product.addSku(sku);
        productRepository.save(product);

        Inventory inventory = Inventory.builder()
                .sku(sku)
                .stockQuantity(10) // 재고 10개
                .build();
        inventoryRepository.save(inventory);
        
        targetSkuId = sku.getId();
    }

    @Test
    @DisplayName("100명이 동시에 10개의 재고를 주문하면 정확히 10명만 성공해야 한다 (Virtual Threads)")
    void concurrentDecreaseStock() throws InterruptedException {
        int threadCount = 100;
        // Java 21 Virtual Threads 사용
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    inventoryService.decreaseStock(targetSkuId, 1);
                    successCount.getAndIncrement();
                } catch (Exception e) {
                    failCount.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 검증: 성공은 정확히 10번이어야 함
        assertThat(successCount.get()).isEqualTo(10);
        
        // 최종 재고 확인
        Inventory finalInventory = inventoryRepository.findBySkuId(targetSkuId).orElseThrow();
        assertThat(finalInventory.getStockQuantity()).isEqualTo(0);
    }
}
