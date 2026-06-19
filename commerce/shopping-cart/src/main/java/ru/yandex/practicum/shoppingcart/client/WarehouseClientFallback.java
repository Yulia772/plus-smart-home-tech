package ru.yandex.practicum.shoppingcart.client;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.interactionapi.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.interactionapi.warehouse.AddressDto;
import ru.yandex.practicum.interactionapi.warehouse.BookedProductsDto;
import ru.yandex.practicum.interactionapi.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.interactionapi.warehouse.api.WarehouseApi;
import ru.yandex.practicum.shoppingcart.exception.BadRequestException;

@Component
public class WarehouseClientFallback implements WarehouseClient {
    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto dto) {
        throw new BadRequestException("Склад временно недоступен");
    }

    @Override
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        throw new BadRequestException("Склад временно недоступен");
    }

    @Override
    public AddressDto getWarehouseAddress() {
        throw new BadRequestException("Склад временно недоступен");
    }

    @Override
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        throw new BadRequestException("Склад временно недоступен");
    }
}
