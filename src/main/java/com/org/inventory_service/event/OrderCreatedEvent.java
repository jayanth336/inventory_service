package com.org.inventory_service.event;

import java.util.List;

public record OrderCreatedEvent(
        Long orderId,
        List<OrderItemEvent> orderItemEventList
) {
}
