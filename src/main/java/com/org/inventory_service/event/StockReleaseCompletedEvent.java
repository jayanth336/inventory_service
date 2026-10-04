package com.org.inventory_service.event;

import com.org.inventory_service.enums.StockReleaseCompletionStatus;

public record StockReleaseCompletedEvent(
        Long orderId,
        StockReleaseCompletionStatus status,
        String reason
) {
}
