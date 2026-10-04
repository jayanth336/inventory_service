package com.org.inventory_service.inventory.repository;

import com.org.inventory_service.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByProductId(Long productId);

    // WE ARE MAINTAINING ATOMICITY BY HAVING QUANTITY CHECK AND UPDATE IN THE SAME QUERY
    // SO THAT WE DON'T RUN INTO A SITUATION WHERE 2 REQUESTS COME VIA KAFKA FOR DIFFERENT ORDERS AND SAME PRODUCT
    // AND BOTH TRY TO UPDATE THE DB
    @Modifying
    @Query("""
        UPDATE Inventory i
        SET i.stockQuantity = i.stockQuantity - :quantity
        WHERE i.productId = :productId
        AND i.stockQuantity >= :quantity""")
    int reduceStockAtomically(@Param("productId") Long id, @Param("quantity") int quantity);
}
