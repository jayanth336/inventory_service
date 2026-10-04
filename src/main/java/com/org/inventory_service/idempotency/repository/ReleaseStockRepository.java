package com.org.inventory_service.idempotency.repository;

import com.org.inventory_service.idempotency.entity.ReleaseStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReleaseStockRepository extends JpaRepository<ReleaseStock, Long> {
}
