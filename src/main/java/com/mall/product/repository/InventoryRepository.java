package com.mall.product.repository;

import com.mall.product.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findBySkuId(Long skuId);

    /**
     * DB Atomic Update: 재고가 충분한 경우에만 차감
     * @return 업데이트된 행의 수 (1이면 성공, 0이면 재고 부족)
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Inventory i SET i.stockQuantity = i.stockQuantity - :qty " +
           "WHERE i.sku.id = :skuId AND i.stockQuantity >= :qty")
    int decreaseStock(@Param("skuId") Long skuId, @Param("qty") int qty);

    /**
     * DB Atomic Update: 재고 복구
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Inventory i SET i.stockQuantity = i.stockQuantity + :qty " +
           "WHERE i.sku.id = :skuId")
    int increaseStock(@Param("skuId") Long skuId, @Param("qty") int qty);
}
