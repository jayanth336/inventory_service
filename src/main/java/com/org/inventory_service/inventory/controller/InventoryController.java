package com.org.inventory_service.inventory.controller;

import com.org.inventory_service.inventory.entity.Inventory;
import com.org.inventory_service.inventory.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Inventory> getInventory(@PathVariable("productId") Long productId) {
        Inventory inventory = inventoryService.getInventory(productId);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        List<Inventory> inventoryList = inventoryService.getInventory();
        return ResponseEntity.ok(inventoryList);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{productId}")
    public ResponseEntity<Void> createInventory(@PathVariable Long productId, @RequestParam int quantity) {
        inventoryService.createInventory(productId, quantity);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{productId}")
    public ResponseEntity<Void> increaseStock(@PathVariable Long productId, @RequestParam int quantity) {
        inventoryService.increaseStock(productId, quantity);
        return ResponseEntity.noContent().build();
    }
}
