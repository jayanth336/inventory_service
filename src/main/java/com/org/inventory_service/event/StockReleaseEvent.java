package com.org.inventory_service.event;

import java.util.List;

public record StockReleaseEvent(
        Long orderId,
        List<OrderItemEvent> orderItemEventList
) {
}
