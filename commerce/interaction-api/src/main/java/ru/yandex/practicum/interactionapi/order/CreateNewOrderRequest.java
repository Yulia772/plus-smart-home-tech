package ru.yandex.practicum.interactionapi.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.interactionapi.warehouse.AddressDto;

@Getter
@Setter
@ToString
public class CreateNewOrderRequest {
    @NotNull
    private ShoppingCartDto shoppingCart;

    @NotNull
    private AddressDto deliveryAddress;

    @NotBlank
    private String username;
}
