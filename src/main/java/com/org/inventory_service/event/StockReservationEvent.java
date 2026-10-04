package com.org.inventory_service.event;

import com.org.inventory_service.enums.StockReservationStatus;

public record StockReservationEvent(
        Long orderId,
        StockReservationStatus status,
        String reason
) {
}
