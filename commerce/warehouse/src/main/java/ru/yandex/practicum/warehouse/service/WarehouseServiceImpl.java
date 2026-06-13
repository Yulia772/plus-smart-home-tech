package ru.yandex.practicum.warehouse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.interactionapi.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.interactionapi.warehouse.AddressDto;
import ru.yandex.practicum.interactionapi.warehouse.BookedProductsDto;
import ru.yandex.practicum.interactionapi.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.warehouse.exception.BadRequestException;
import ru.yandex.practicum.warehouse.mapper.WarehouseMapper;
import ru.yandex.practicum.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.warehouse.repository.WarehouseProductRepository;

import java.security.SecureRandom;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {
    private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS = ADDRESSES[new SecureRandom().nextInt(ADDRESSES.length)];
    private final WarehouseProductRepository warehouseProductRepository;
    private final WarehouseMapper warehouseMapper;

    @Override
    @Transactional(readOnly = true)
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto dto) {
        List<UUID> productIds = validateRequestAndExtractProductIds(dto);

        List<WarehouseProduct> warehouseProducts = warehouseProductRepository.findAllByProductIdIn(productIds);

        Map<UUID, WarehouseProduct> warehouseProductsById = warehouseProducts.stream()
                .collect(Collectors.toMap(
                        WarehouseProduct::getProductId,
                        warehouseProduct -> warehouseProduct
                ));

        double deliveryWeight = 0.0;
        double deliveryVolume = 0.0;
        boolean fragile = false;

        for (Map.Entry<UUID, Long> entry : dto.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long requiredQuantity = entry.getValue();

            WarehouseProduct warehouseProduct = warehouseProductsById.get(productId);

            if (warehouseProduct == null) {
                throw new BadRequestException(
                        "Товара нет на складе: productId=" + productId
                                + ", требуется=" + requiredQuantity
                                + ", доступно=0"
                                + ", не хватает=" + requiredQuantity
                );
            }

            Long availableQuantity = warehouseProduct.getQuantity();
            if (availableQuantity < requiredQuantity) {
                throw new BadRequestException(
                        "Недостаточно товара на складе: productId=" + productId
                                + ", требуется=" + requiredQuantity
                                + ", доступно=" + availableQuantity
                                + ", не хватает=" + (requiredQuantity - availableQuantity)
                );
            }

            deliveryWeight += warehouseProduct.getWeight() * requiredQuantity;
            deliveryVolume += warehouseProduct.getDepth()
                    * warehouseProduct.getHeight()
                    * warehouseProduct.getWidth()
                    * requiredQuantity;

            if (warehouseProduct.getFragile()) {
                fragile = true;
            }
        }
        log.info("Корзина проверена на складе: productsCount={}, deliveryWeight={}, deliveryVolume={}, fragile={}",
                productIds.size(), deliveryWeight, deliveryVolume, fragile);

        BookedProductsDto bookedProductsDto = new BookedProductsDto();
        bookedProductsDto.setDeliveryVolume(deliveryVolume);
        bookedProductsDto.setFragile(fragile);
        bookedProductsDto.setDeliveryWeight(deliveryWeight);

        return bookedProductsDto;
    }

    @Override
    @Transactional
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        if (request == null) {
            throw new BadRequestException("Передано неверное значение");
        }
        UUID productId = request.getProductId();
        if (productId == null) {
            throw new BadRequestException("Передано неверное значение");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BadRequestException("Передано неверное значение");
        }
        WarehouseProduct product = warehouseProductRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException("Товар с таким id на складе не зарегистрирован"));
        Long oldQuantity = product.getQuantity();
        Long newQuantity = oldQuantity + request.getQuantity();
        product.setQuantity(newQuantity);
        warehouseProductRepository.save(product);
        log.info("Количество товара на складе обновлено: productId={}, oldQuantity={}, addedQuantity={}, newQuantity={}",
                productId, oldQuantity, request.getQuantity(), newQuantity);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDto getWarehouseAddress() {
        AddressDto address = new AddressDto();
        address.setCountry(CURRENT_ADDRESS);
        address.setCity(CURRENT_ADDRESS);
        address.setStreet(CURRENT_ADDRESS);
        address.setHouse(CURRENT_ADDRESS);
        address.setFlat(CURRENT_ADDRESS);
        return address;
    }

    @Override
    @Transactional
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        if (request == null) {
            throw new BadRequestException("Передано неверное значение");
        }
        UUID productId = request.getProductId();
        if (productId == null) {
            throw new BadRequestException("Передано неверное значение");
        }
        if (warehouseProductRepository.existsById(productId)) {
            throw new BadRequestException("Товар с таким id уже зарегистрирован на складе");
        }
        WarehouseProduct product = warehouseMapper.toProduct(request);
        product.setQuantity(0L);
        if (product.getFragile() == null) {
            product.setFragile(false);
        }
        warehouseProductRepository.save(product);
        log.info("Товар зарегистрирован на складе: productId={}", product.getProductId());
    }
    
    private List<UUID> validateRequestAndExtractProductIds(ShoppingCartDto dto) {
        if (dto == null) {
            throw new BadRequestException("Корзина не может быть пустой");
        }
        Map<UUID, Long> products = dto.getProducts();
        if (products == null || products.isEmpty()) {
            throw new BadRequestException("Список товаров в корзине не может быть пустым");
        }
        List<UUID> productIds = new ArrayList<>();

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long requiredQuantity = entry.getValue();

            if (productId == null) {
                throw new BadRequestException("Идентификатор товара не может быть пустым");
            }

            if (requiredQuantity == null || requiredQuantity <= 0) {
                throw new BadRequestException("Количество товара должно быть положительным: productId=" + productId);
            }
            productIds.add(productId);
        }
        return productIds;
    }
}
