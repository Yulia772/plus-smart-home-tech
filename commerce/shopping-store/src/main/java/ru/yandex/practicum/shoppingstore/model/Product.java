package ru.yandex.practicum.shoppingstore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.interactionapi.store.ProductCategory;
import ru.yandex.practicum.interactionapi.store.ProductState;
import ru.yandex.practicum.interactionapi.store.QuantityState;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products", schema = "shopping_store")
@Getter
@Setter
public class Product {
    @Id
    private UUID productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "image_src")
    private String imageSrc;

    @Enumerated(EnumType.STRING)
    @Column(name = "quantity_state", nullable = false)
    private QuantityState quantityState;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_state", nullable = false)
    private ProductState productState;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_category")
    private ProductCategory productCategory;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;
}
