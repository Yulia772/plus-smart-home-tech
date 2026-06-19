package ru.yandex.practicum.warehouse.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.interactionapi.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.interactionapi.warehouse.AddressDto;
import ru.yandex.practicum.interactionapi.warehouse.BookedProductsDto;
import ru.yandex.practicum.interactionapi.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.interactionapi.warehouse.api.WarehouseApi;
import ru.yandex.practicum.warehouse.service.WarehouseService;

@RestController
@RequiredArgsConstructor
public class WarehouseController implements WarehouseApi {
    private final WarehouseService warehouseService;
    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto dto) {
        return warehouseService.checkProductQuantityEnoughForShoppingCart(dto);
    }

    @Override
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        warehouseService.addProductToWarehouse(request);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return warehouseService.getWarehouseAddress();
    }

    @Override
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        warehouseService.newProductInWarehouse(request);
    }
}
