package com.org.inventory_service.inventory.exception;

public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(Long productId) {
        super("Product with id " + productId + " not found");
    }
}
