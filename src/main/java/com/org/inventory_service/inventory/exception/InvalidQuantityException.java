package com.org.inventory_service.inventory.exception;

public class InvalidQuantityException extends RuntimeException {
    public InvalidQuantityException() {
        super("Quantity must be greater than 0");
    }
}
