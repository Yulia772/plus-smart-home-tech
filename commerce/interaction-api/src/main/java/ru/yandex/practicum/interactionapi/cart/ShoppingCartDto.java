package ru.yandex.practicum.interactionapi.cart;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString
public class ShoppingCartDto {
    @NotNull
    private UUID shoppingCartId;
    @NotNull
    private Map<UUID, Long> products;
}
