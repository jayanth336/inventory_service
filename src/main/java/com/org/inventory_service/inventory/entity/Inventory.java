package com.org.inventory_service.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(nullable = false, unique = true)
    private Long productId;
    /*
    BEFORE : ONE PRODUCT SHOULD HAVE ONE INVENTORY. SO IT IS ONE-TO-ONE MAPPING. INVENTORY CANNOT EXIST WITHOUT PRODUCT.
    SO I STORE PRODUCT_ID FOREIGN KEY IN INVENTORY.
    AFTER SEPARATING INVENTORY AS A SEPARATE MICROSERVICE, WE DON'T MAINTAIN THE DATABASE FOREIGN KEY ACROSS SERVICES.

     */

    @Column(nullable = false)
    private int stockQuantity;
}
