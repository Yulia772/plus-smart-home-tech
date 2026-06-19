package ru.yandex.practicum.interactionapi.warehouse.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.interactionapi.warehouse.*;

public interface WarehouseApi {

    @PostMapping("/api/v1/warehouse/check")
    BookedProductsDto checkProductQuantityEnoughForShoppingCart(@Valid @RequestBody ShoppingCartDto dto);

    @PostMapping("/api/v1/warehouse/add")
    void addProductToWarehouse(@Valid @RequestBody AddProductToWarehouseRequest request);

    @GetMapping("/api/v1/warehouse/address")
    AddressDto getWarehouseAddress();

    @PutMapping("/api/v1/warehouse")
    void newProductInWarehouse(@Valid @RequestBody NewProductInWarehouseRequest request);
}
