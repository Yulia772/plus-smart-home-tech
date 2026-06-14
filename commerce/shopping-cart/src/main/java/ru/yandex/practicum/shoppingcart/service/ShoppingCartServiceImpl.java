package ru.yandex.practicum.shoppingcart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.interactionapi.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.shoppingcart.client.WarehouseClient;
import ru.yandex.practicum.shoppingcart.exception.BadRequestException;
import ru.yandex.practicum.shoppingcart.exception.NotFoundException;
import ru.yandex.practicum.shoppingcart.mapper.ShoppingCartMapper;
import ru.yandex.practicum.shoppingcart.model.ShoppingCart;
import ru.yandex.practicum.shoppingcart.repository.ShoppingCartRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartMapper shoppingCartMapper;
    private final ShoppingCartRepository shoppingCartRepository;
    private final WarehouseClient warehouseClient;

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        ShoppingCart shoppingCart = getOrCreateActiveCart(username);
        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        if (products == null || products.isEmpty()) {
            throw new BadRequestException("Список товаров не может быть пустым");
        }
        ShoppingCart shoppingCart = getOrCreateActiveCart(username);
        Map<UUID, Long> cartProducts = shoppingCart.getProducts();

        Map<UUID, Long> futureProducts = new HashMap<>(cartProducts);

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();

            if (productId == null) {
                throw new BadRequestException("Id товара не может быть пустым");
            }
            if (quantity == null || quantity <= 0) {
                throw new BadRequestException("Количество товара должно быть положительным");
            }
            if (futureProducts.containsKey(productId)) {
                Long oldQuantity = futureProducts.get(productId);
                futureProducts.put(productId, oldQuantity + quantity);
            } else {
                futureProducts.put(productId, quantity);
            }
        }
        ShoppingCartDto cartForWarehouseCheck = new ShoppingCartDto();
        cartForWarehouseCheck.setShoppingCartId(shoppingCart.getShoppingCartId());
        cartForWarehouseCheck.setProducts(futureProducts);

        warehouseClient.checkProductQuantityEnoughForShoppingCart(cartForWarehouseCheck);

        cartProducts.clear();
        cartProducts.putAll(futureProducts);

        ShoppingCart newShoppingCart = shoppingCartRepository.save(shoppingCart);
        return shoppingCartMapper.toDto(newShoppingCart);
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> products) {
        if (products == null || products.isEmpty()) {
            throw new BadRequestException("Список не должен быть пустым");
        }
        ShoppingCart shoppingCart = getActiveCart(username);
        Map<UUID, Long> cartProducts = shoppingCart.getProducts();
        for (UUID productId : products) {
            if (productId == null) {
                throw new BadRequestException("Id товара не может быть пустым");
            }
            if (!cartProducts.containsKey(productId)) {
                throw new NotFoundException("Товар с таким id не найден в корзине");
            }
            cartProducts.remove(productId);
        }
        ShoppingCart savedCart = shoppingCartRepository.save(shoppingCart);
        return shoppingCartMapper.toDto(savedCart);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        if (request == null) {
            throw new BadRequestException("Запрос не может быть пустым");
        }
        UUID productId = request.getProductId();
        Long newQuantity = request.getNewQuantity();
        if (productId == null) {
            throw new BadRequestException("Id товара не может быть пустым");
        }

        if (newQuantity == null || newQuantity <= 0) {
            throw new BadRequestException("Количество товара должно быть положительным");
        }
        ShoppingCart shoppingCart = getActiveCart(username);
        Map<UUID, Long> products = shoppingCart.getProducts();

        if (!products.containsKey(productId)) {
            throw new NotFoundException("Товар с таким id не найден в корзине");
        }
        products.put(productId, newQuantity);
        ShoppingCart savedCart = shoppingCartRepository.save(shoppingCart);
        return shoppingCartMapper.toDto(savedCart);
    }

    @Override
    public void deactivateCurrentShoppingCart(String username) {
        Optional<ShoppingCart> shoppingCart = shoppingCartRepository.findByUsernameAndActiveTrue(username);
        if (shoppingCart.isPresent()) {
            ShoppingCart findShoppingCart = shoppingCart.get();
            findShoppingCart.setActive(false);
            shoppingCartRepository.save(findShoppingCart);
        }
    }

    private ShoppingCart getOrCreateActiveCart(String username) {
        return shoppingCartRepository.findByUsernameAndActiveTrue(username)
                .orElseGet(() -> createNewCart(username));
    }

    private ShoppingCart createNewCart(String username) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setShoppingCartId(UUID.randomUUID());
        shoppingCart.setUsername(username);
        shoppingCart.setActive(true);
        return shoppingCartRepository.save(shoppingCart);
    }

    private ShoppingCart getActiveCart(String username) {
        return shoppingCartRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new NotFoundException("Корзина не найдена"));
    }
}
