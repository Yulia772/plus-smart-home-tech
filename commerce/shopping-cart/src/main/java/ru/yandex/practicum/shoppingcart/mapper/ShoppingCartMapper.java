package ru.yandex.practicum.shoppingcart.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.shoppingcart.model.ShoppingCart;

@Mapper(componentModel = "spring")
public interface ShoppingCartMapper {
    ShoppingCartDto toDto(ShoppingCart shoppingCart);
}
