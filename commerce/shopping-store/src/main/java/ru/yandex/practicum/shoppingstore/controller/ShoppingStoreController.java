package ru.yandex.practicum.shoppingstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.interactionapi.store.ProductCategory;
import ru.yandex.practicum.interactionapi.store.ProductDto;
import ru.yandex.practicum.interactionapi.store.QuantityState;
import ru.yandex.practicum.interactionapi.store.api.ShoppingStoreApi;
import ru.yandex.practicum.shoppingstore.service.ShoppingStoreService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ShoppingStoreController implements ShoppingStoreApi {
    private final ShoppingStoreService shoppingStoreService;

    @Override
    public Page<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        return shoppingStoreService.getProducts(category, pageable);
    }

    @Override
    public ProductDto createNewProduct(ProductDto productDto) {
        return shoppingStoreService.createNewProduct(productDto);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        return shoppingStoreService.updateProduct(productDto);
    }

    @Override
    public Boolean removeProductFromStore(UUID productId) {
        return shoppingStoreService.removeProductFromStore(productId);
    }

    @Override
    public Boolean setProductQuantityState(UUID productId, QuantityState quantityState) {
        return shoppingStoreService.setProductQuantityState(productId, quantityState);
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        return shoppingStoreService.getProduct(productId);
    }
}
