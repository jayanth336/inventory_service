package com.org.inventory_service.kafka;

import com.org.inventory_service.enums.StockReservationStatus;
import com.org.inventory_service.event.OrderCreatedEvent;
import com.org.inventory_service.event.OrderItemEvent;
import com.org.inventory_service.event.StockReservationEvent;
import com.org.inventory_service.inventory.exception.InsufficientStockException;
import com.org.inventory_service.inventory.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderEventConsumer {
    private final InventoryService inventoryService;
    private final StockReservationEventProducer  stockReservationEventProducer;

    public OrderEventConsumer(InventoryService inventoryService, StockReservationEventProducer stockReservationEventProducer) {
        this.inventoryService = inventoryService;
        this.stockReservationEventProducer = stockReservationEventProducer;
    }

    // LET'S SAY IF AN EXCEPTION FOR DATABASE IS THROWN
    // IT'S NOT CAUGHT IN CATCH BLOCK. BECAUSE CATCH BLOCK IS ONLY FOR InsufficientStockException
    // SO IT GOES OUT OF THE CONSUME METHOD
    // REACHES KAFKA'S ERROR HANDLING/ RETRY MECHANISM
    // SO THE RETRY HAPPENS FOR THAT PARTICULAR ORDER ID
    @KafkaListener(topics = "order-events", groupId = "inventory-service")
    public void consume(OrderCreatedEvent orderCreatedEvent) {
        System.out.println("Order event received: " + orderCreatedEvent);
        StockReservationEvent event;

        try {
            inventoryService.reserveStock(orderCreatedEvent);
            event = new StockReservationEvent(orderCreatedEvent.orderId(),
                    StockReservationStatus.SUCCESS,
                    null);
            stockReservationEventProducer.publishStockReservationEvent(event);
        } catch (InsufficientStockException exception) {
             event = new StockReservationEvent(orderCreatedEvent.orderId(),
                    StockReservationStatus.FAILED,
                    exception.getLocalizedMessage());
            stockReservationEventProducer.publishStockReservationEvent(event);
        }
    }
}
