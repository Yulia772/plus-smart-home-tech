package ru.yandex.practicum.interactionapi.store.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interactionapi.store.ProductCategory;
import ru.yandex.practicum.interactionapi.store.ProductDto;
import ru.yandex.practicum.interactionapi.store.QuantityState;

import java.util.UUID;

public interface ShoppingStoreApi {

    @GetMapping("/api/v1/shopping-store")
    Page<ProductDto> getProducts(@RequestParam("category") ProductCategory category, Pageable pageable);

    @PutMapping("/api/v1/shopping-store")
    ProductDto createNewProduct(@Valid @RequestBody ProductDto productDto);

    @PostMapping("/api/v1/shopping-store")
    ProductDto updateProduct(@Valid @RequestBody ProductDto productDto);

    @PostMapping("/api/v1/shopping-store/removeProductFromStore")
    Boolean removeProductFromStore(@RequestBody UUID productId);

    @PostMapping("/api/v1/shopping-store/quantityState")
    Boolean setProductQuantityState(@RequestParam("productId") UUID productId,
                                    @RequestParam("quantityState") QuantityState quantityState);

    @GetMapping("/api/v1/shopping-store/{productId}")
    ProductDto getProduct(@PathVariable("productId") UUID productId);
}
