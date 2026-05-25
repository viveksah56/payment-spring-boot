package com.backend.Entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Product extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID productId;

    private String name;
    @Column(length = 100, columnDefinition = "TEXT")
    private String description;
    private double price;
    @Column(nullable = true)
    private Double discount;

}
