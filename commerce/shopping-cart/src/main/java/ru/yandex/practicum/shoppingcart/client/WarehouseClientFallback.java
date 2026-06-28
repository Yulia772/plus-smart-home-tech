package ru.yandex.practicum.shoppingcart.client;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.interactionapi.warehouse.*;
import ru.yandex.practicum.shoppingcart.exception.BadRequestException;

import java.util.Map;
import java.util.UUID;

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

    @Override
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        throw new BadRequestException("Склад временно недоступен");
    }

    @Override
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        throw new BadRequestException("Склад временно недоступен");
    }

    @Override
    public void acceptReturn(Map<UUID, Long> products) {
        throw new BadRequestException("Склад временно недоступен");
    }
}
