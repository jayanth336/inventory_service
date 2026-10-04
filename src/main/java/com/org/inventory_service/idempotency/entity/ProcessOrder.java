package com.org.inventory_service.idempotency.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ProcessOrder {
    @Id
    private Long orderId;
    private LocalDateTime processedAt;

    public ProcessOrder(Long orderId) {
        this.orderId = orderId;
        this.processedAt = LocalDateTime.now();
    }
}
