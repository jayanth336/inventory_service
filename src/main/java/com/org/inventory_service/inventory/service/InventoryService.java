package com.org.inventory_service.inventory.service;

import com.org.inventory_service.event.OrderCreatedEvent;
import com.org.inventory_service.event.OrderItemEvent;
import com.org.inventory_service.event.StockReleaseEvent;
import com.org.inventory_service.idempotency.entity.ProcessOrder;
import com.org.inventory_service.idempotency.entity.ReleaseStock;
import com.org.inventory_service.idempotency.repository.ProcessOrderRepository;
import com.org.inventory_service.idempotency.repository.ReleaseStockRepository;
import com.org.inventory_service.inventory.entity.Inventory;
import com.org.inventory_service.inventory.exception.InsufficientStockException;
import com.org.inventory_service.inventory.exception.InvalidQuantityException;
import com.org.inventory_service.inventory.exception.InventoryNotFoundException;
import com.org.inventory_service.inventory.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProcessOrderRepository processOrderRepository;
    private final ReleaseStockRepository releaseStockRepository;

    public InventoryService(InventoryRepository inventoryRepository, ProcessOrderRepository processOrderRepository,
                            ReleaseStockRepository releaseStockRepository) {
        this.inventoryRepository = inventoryRepository;
        this.processOrderRepository = processOrderRepository;
        this.releaseStockRepository = releaseStockRepository;
    }

    /**
     * reserveStock() handles the whole order; reduceStock() handles one product's stock.
     * @Transactional on reserveStock() ensures all products update or all roll back if anything fails.
     * Custom UPDATE does stock check + stock reduction atomically in one DB operation - or in 1 DB query.
     * This prevents two concurrent orders from overselling the same stock.
     */
    @Transactional
    public void reserveStock(OrderCreatedEvent orderCreatedEvent) {
        // CHECK IF THE ORDER IS ALREADY PROCESSED - IDEMPOTENCY
        Long orderId = orderCreatedEvent.orderId();
        if(processOrderRepository.existsById(orderId)) {
            return;
        }

        // IF NOT, THEN IT MEANS WE HAVEN'T PROCESSED THE ORDER YET
        // SO PROCESS IT
        List<OrderItemEvent> orderItemEventList = orderCreatedEvent.orderItemEventList();

        for (OrderItemEvent orderItemEvent : orderItemEventList) {
            reduceStock(orderItemEvent.productId(), orderItemEvent.quantity());
        }

        // AFTER PROCESSING, MARK THE ORDER AS PROCESSED
        ProcessOrder processOrder = new ProcessOrder(orderId);
        processOrderRepository.save(processOrder);

        /**
         * Let's say there 30 items in cart. And the 30th item is out of stock.
         * Now what happens?
         *  - reduceStock() throws InsufficientStockException for the 30th item.
         *  - Exception propagates from reduceStock() → reserveStock().
         *  - @Transactional on reserveStock() causes all 29 previous inventory updates to roll back.
         *  - ProcessOrder is not saved, because that line is after the loop and is never reached.
         *  - Exception propagates out of reserveStock() → OrderEventConsumer.
         *  - Your catch (InsufficientStockException) catches it.
         *  - StockReservationEventProducer produces FAILED event.
         *  - Ecommerce consumes the failed event and sets STOCK_RESERVATION_FAILED.
         */
    }

    // THIS METHOD FOR CUSTOMER END - BECAUSE CUSTOMER CAN ONLY BE ABLE TO REDUCE THE STOCK THROUGH ORDERING
    @Transactional
    public void reduceStock(Long productId, int quantity) {
        if (quantity <= 0) throw new InvalidQuantityException();

        int updatedRows =  inventoryRepository.reduceStockAtomically(productId, quantity);

        if(updatedRows == 0) {
            throw new InsufficientStockException(productId, quantity);
        }

        // NO NEED TO SAVE IT IN INVENTORY_REPO EXPLICITLY BECAUSE WE ARE DIRECTLY USING UPDATE QUERY
    }

    @Transactional
    public void restoreStock(StockReleaseEvent stockReleaseEvent) {
        // CHECK IF THE ORDER IS ALREADY PROCESSED - IDEMPOTENCY
        Long orderId = stockReleaseEvent.orderId();
        if(releaseStockRepository.existsById(orderId)) {
            return;
        }

        // IF NOT, THEN IT MEANS WE HAVEN'T PROCESSED THE ORDER YET
        // SO PROCESS IT
        List<OrderItemEvent> orderItemEventList = stockReleaseEvent.orderItemEventList();
        for(OrderItemEvent orderItemEvent : orderItemEventList){
            releaseStock(orderItemEvent.productId(),  orderItemEvent.quantity());
        }

        // AFTER PROCESSING, MARK THE ORDER AS PROCESSED
        ReleaseStock releaseStock = new ReleaseStock(orderId);
        releaseStockRepository.save(releaseStock);

        /**
         * Handles the scenario where payment fails for an order.
         *
         * Flow:
         *  - Payment status is set to FAILED.
         *  - A StockReleaseEvent is created containing the orderId and the list of ordered products with their quantities.
         *  - The StockReleaseEvent is published to Kafka.
         *  - Inventory Service consumes the event and restores the stock that was previously reserved for the order.
         *  - After successfully restoring all items, Inventory Service publishes a StockReleaseCompletedEvent.
         *  - Ecommerce Service consumes the completion event and updates the order to its final FAILED state.
         *
         * This is the compensation step of our Choreography-based Saga.
         */

    }

    @Transactional
    public void releaseStock(Long productId, int quantity) {
        if(quantity <= 0) throw new InvalidQuantityException();
        Inventory inventory = inventoryRepository.findByProductId(productId).orElseThrow(() -> new InventoryNotFoundException(productId));
        inventory.setStockQuantity(inventory.getStockQuantity() + quantity);

        // NO NEED TO SAVE IT IN REPOSITORY, BECAUSE INVENTORY HAS BECOME PART OF JPA MANAGED ENTITY AFTER ADDING @TRANSACTIONAL
        // DIRTY CHECKING TAKES CARE OF IT
    }

    // THIS METHOD IS FOR ADMIN END - TO ADD PRODUCT WITH THE INITIAL STOCK TO THE INVENTORY
    public void createInventory(Long productId, int initialStock) {
        if (initialStock < 0) {
            throw new InvalidQuantityException();
        }

        Inventory newInventory = new Inventory();
        newInventory.setProductId(productId);
        newInventory.setStockQuantity(initialStock);

        inventoryRepository.save(newInventory);
    }

    // THIS METHOD IS FOR ADMIN END - TO STOCK UP THE EXISTING PRODUCT
    @Transactional
    public void increaseStock(Long productId, int quantity) {
        if(quantity <= 0) throw new InvalidQuantityException();
        Inventory inventory = inventoryRepository.findByProductId(productId).orElseThrow(() -> new InventoryNotFoundException(productId)); //----- LINE 1
        inventory.setStockQuantity(inventory.getStockQuantity() + quantity);

        // NO NEED TO SAVE INVENTORY IN INVENTORY_REPO - BECAUSE INVENTORY BECAME PART OF JPA MANAGED ENTITY IN LINE 1
    }

    public Inventory getInventory(Long productId) {
        return inventoryRepository.findByProductId(productId).orElseThrow(() -> new InventoryNotFoundException(productId));
    }

    public List<Inventory> getInventory() {
        return inventoryRepository.findAll();
    }
}
