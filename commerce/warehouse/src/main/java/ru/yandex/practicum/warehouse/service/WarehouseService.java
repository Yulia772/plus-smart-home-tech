package ru.yandex.practicum.warehouse.service;

import ru.yandex.practicum.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.interactionapi.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.interactionapi.warehouse.AddressDto;
import ru.yandex.practicum.interactionapi.warehouse.BookedProductsDto;
import ru.yandex.practicum.interactionapi.warehouse.NewProductInWarehouseRequest;

public interface WarehouseService {

    BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto dto);

    void addProductToWarehouse(AddProductToWarehouseRequest request);

    AddressDto getWarehouseAddress();

    void newProductInWarehouse(NewProductInWarehouseRequest request);
}
