package ru.yandex.practicum.warehouse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "products", schema = "warehouse")
@Getter
@Setter
public class WarehouseProduct {
    @Id
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private Boolean fragile;

    @Column(nullable = false)
    private Double width;

    @Column(nullable = false)
    private Double height;

    @Column(nullable = false)
    private Double depth;

    @Column(nullable = false)
    private Double weight;
}
