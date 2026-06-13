package ru.yandex.practicum.shoppingcart.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "shopping_carts", schema = "shopping_cart")
@Getter
@Setter
public class ShoppingCart {
    @Id
    @Column(name = "shopping_cart_id")
    private UUID shoppingCartId;
    @Column(name = "username", nullable = false)
    private String username;
    @Column(name = "active", nullable = false)
    private boolean active;

    @ElementCollection
    @CollectionTable(
            name = "cart_products",
            schema = "shopping_cart",
            joinColumns = @JoinColumn(name = "shopping_cart_id")
    )
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity", nullable = false)
    private Map<UUID, Long> products = new HashMap<>();
}
