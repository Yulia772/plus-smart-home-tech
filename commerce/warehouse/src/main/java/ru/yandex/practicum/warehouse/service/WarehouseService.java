package ru.yandex.practicum.warehouse.service;

import ru.yandex.practicum.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.interactionapi.warehouse.*;

import java.util.Map;
import java.util.UUID;

public interface WarehouseService {

    BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto dto);

    void addProductToWarehouse(AddProductToWarehouseRequest request);

    AddressDto getWarehouseAddress();

    void newProductInWarehouse(NewProductInWarehouseRequest request);

    BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request);

    void shippedToDelivery(ShippedToDeliveryRequest request);

    void acceptReturn(Map<UUID, Long> products);
}
