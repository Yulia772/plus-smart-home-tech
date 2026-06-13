package ru.yandex.practicum.shoppingstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.interactionapi.store.ProductCategory;
import ru.yandex.practicum.interactionapi.store.ProductDto;
import ru.yandex.practicum.interactionapi.store.QuantityState;

import java.util.UUID;

public interface ShoppingStoreService {
    Page<ProductDto> getProducts(ProductCategory category, Pageable pageable);

    ProductDto createNewProduct(ProductDto productDto);

    ProductDto updateProduct(ProductDto productDto);

    Boolean removeProductFromStore(UUID productId);

    Boolean setProductQuantityState(UUID productId, QuantityState quantityState);

    ProductDto getProduct(UUID productId);
}
