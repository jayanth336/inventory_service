package com.org.inventory_service.inventory.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(Long productId, int availableStock) {
        super("Only " + availableStock + " units of " + productId + " are available");
    }
}
