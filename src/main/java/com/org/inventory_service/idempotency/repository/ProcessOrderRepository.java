package com.org.inventory_service.idempotency.repository;

import com.org.inventory_service.idempotency.entity.ProcessOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessOrderRepository extends JpaRepository<ProcessOrder, Long> {
}
