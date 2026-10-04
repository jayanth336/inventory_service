package com.org.inventory_service.event;

public record OrderItemEvent(
        Long productId,
        int quantity
) {
}
