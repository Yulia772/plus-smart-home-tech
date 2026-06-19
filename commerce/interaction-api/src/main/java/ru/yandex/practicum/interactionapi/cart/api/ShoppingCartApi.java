package ru.yandex.practicum.interactionapi.cart.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interactionapi.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.interactionapi.cart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartApi {

    @GetMapping("/api/v1/shopping-cart")
    ShoppingCartDto getShoppingCart(@RequestParam("username") String username);

    @PutMapping("/api/v1/shopping-cart")
    ShoppingCartDto addProductToShoppingCart(@RequestParam("username") String username,
                                             @RequestBody Map<UUID, Long> products);

    @PostMapping("/api/v1/shopping-cart/remove")
    ShoppingCartDto removeFromShoppingCart(@RequestParam("username") String username,
                                           @RequestBody List<UUID> products);

    @PostMapping("/api/v1/shopping-cart/change-quantity")
    ShoppingCartDto changeProductQuantity(@RequestParam("username") String username,
                                          @Valid @RequestBody ChangeProductQuantityRequest request);

    @DeleteMapping("/api/v1/shopping-cart")
    void deactivateCurrentShoppingCart(@RequestParam("username") String username);
}
