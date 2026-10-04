package com.org.inventory_service.kafka;

import com.org.inventory_service.enums.StockReleaseCompletionStatus;
import com.org.inventory_service.event.StockReleaseCompletedEvent;
import com.org.inventory_service.event.StockReleaseEvent;
import com.org.inventory_service.inventory.exception.InventoryNotFoundException;
import com.org.inventory_service.inventory.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class StockReleaseEventConsumer {
    private final InventoryService inventoryService;
    private final StockReleaseCompletionProducer  stockReleaseCompletionProducer;

    public StockReleaseEventConsumer(InventoryService inventoryService, StockReleaseCompletionProducer stockReleaseCompletionProducer) {
        this.inventoryService = inventoryService;
        this.stockReleaseCompletionProducer = stockReleaseCompletionProducer;
    }

    @KafkaListener(topics = "stock-release-events", groupId = "inventory-service")
    public void consume(StockReleaseEvent stockReleaseEvent) {
        System.out.println("Received stock release event: " + stockReleaseEvent);
        StockReleaseCompletedEvent completedEvent;

        try {
            inventoryService.restoreStock(stockReleaseEvent);
            completedEvent = new StockReleaseCompletedEvent(
                    stockReleaseEvent.orderId(),
                    StockReleaseCompletionStatus.STOCK_RELEASE_COMPLETED,
                    null
            );
            stockReleaseCompletionProducer.publishStockReleaseCompletedEvent(completedEvent);
        } catch (InventoryNotFoundException exception) {
            completedEvent = new StockReleaseCompletedEvent(
                    stockReleaseEvent.orderId(),
                    StockReleaseCompletionStatus.STOCK_RELEASE_FAILED,
                    exception.getMessage()
            );
            stockReleaseCompletionProducer.publishStockReleaseCompletedEvent(completedEvent);
        }
    }
}
