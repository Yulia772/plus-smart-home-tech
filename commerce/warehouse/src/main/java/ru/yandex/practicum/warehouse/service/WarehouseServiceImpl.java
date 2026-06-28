package ru.yandex.practicum.warehouse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.interactionapi.warehouse.*;
import ru.yandex.practicum.warehouse.exception.BadRequestException;
import ru.yandex.practicum.warehouse.exception.NotFoundException;
import ru.yandex.practicum.warehouse.mapper.WarehouseMapper;
import ru.yandex.practicum.warehouse.model.OrderBooking;
import ru.yandex.practicum.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.warehouse.repository.OrderBookingRepository;
import ru.yandex.practicum.warehouse.repository.WarehouseProductRepository;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {
    private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS = ADDRESSES[new SecureRandom().nextInt(ADDRESSES.length)];
    private final WarehouseProductRepository warehouseProductRepository;
    private final WarehouseMapper warehouseMapper;
    private final OrderBookingRepository orderBookingRepository;

    @Override
    @Transactional(readOnly = true)
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto dto) {
        if (dto == null) {
            throw new BadRequestException("Корзина не может быть пустой");
        }
        Map<UUID, Long> products = dto.getProducts();
        List<UUID> productIds = validateProductsAndExtractProductIds(products);
        Map<UUID, WarehouseProduct> warehouseProductsById = getWarehouseProductsById(productIds);

        BookedProductsDto bookedProductsDto =
                checkAndCalculateBookedProducts(products, warehouseProductsById, false);
        log.info("Корзина проверена на складе: productsCount={}, deliveryWeight={}, deliveryVolume={}, fragile={}",
                productIds.size(),
                bookedProductsDto.getDeliveryWeight(),
                bookedProductsDto.getDeliveryVolume(),
                bookedProductsDto.getFragile());

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

    @Override
    @Transactional
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        if (request == null) {
            throw new BadRequestException("Передано неверное значение");
        }
        if (request.getOrderId() == null) {
            throw new BadRequestException("Передано неверное значение");
        }
        Map<UUID, Long> products = request.getProducts();
        List<UUID> productIds = validateProductsAndExtractProductIds(products);
        if (orderBookingRepository.existsById(request.getOrderId())) {
            throw new BadRequestException("Такой заказ уже собран");
        }

        Map<UUID, WarehouseProduct> warehouseProductsById = getWarehouseProductsById(productIds);
        BookedProductsDto bookedProductsDto =
                checkAndCalculateBookedProducts(products, warehouseProductsById, true);

        OrderBooking booking = new OrderBooking();
        booking.setOrderId(request.getOrderId());
        booking.setProducts(new HashMap<>(products));
        booking.setDeliveryWeight(bookedProductsDto.getDeliveryWeight());
        booking.setDeliveryVolume(bookedProductsDto.getDeliveryVolume());
        booking.setFragile(bookedProductsDto.getFragile());
        booking.setDeliveryId(null);
        orderBookingRepository.save(booking);

        log.info("Заказ собран на складе: orderId={}, productsCount={}, deliveryWeight={}, deliveryVolume={}, fragile={}",
                request.getOrderId(),
                productIds.size(),
                bookedProductsDto.getDeliveryWeight(),
                bookedProductsDto.getDeliveryVolume(),
                bookedProductsDto.getFragile());

        return bookedProductsDto;
    }

    @Override
    @Transactional
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        if (request == null) {
            throw new BadRequestException("Передано неверное значение");
        }
        if (request.getOrderId() == null) {
            throw new BadRequestException("Передано неверное значение");
        }
        if (request.getDeliveryId() == null) {
            throw new BadRequestException("Передано неверное значение");
        }
        OrderBooking orderBooking = orderBookingRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));
        orderBooking.setDeliveryId(request.getDeliveryId());
        orderBookingRepository.save(orderBooking);
        log.info("Заказ передан в доставку: orderId={}, deliveryId={}",
                request.getOrderId(), request.getDeliveryId());
    }

    @Override
    @Transactional
    public void acceptReturn(Map<UUID, Long> products) {
        List<UUID> productIds = validateProductsAndExtractProductIds(products);
        Map<UUID, WarehouseProduct> warehouseProductsById = getWarehouseProductsById(productIds);

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long returnedQuantity = entry.getValue();

            WarehouseProduct warehouseProduct = warehouseProductsById.get(productId);

            if (warehouseProduct == null) {
                throw new BadRequestException(
                        "Товар с таким id не зарегистрирован на складе: productId=" + productId);
            }
            warehouseProduct.setQuantity(warehouseProduct.getQuantity() + returnedQuantity);
        }
        log.info("Возврат принят на склад: productsCount={}", productIds.size());
    }
    
    private List<UUID> validateProductsAndExtractProductIds(Map<UUID, Long> products) {
        if (products == null || products.isEmpty()) {
            throw new BadRequestException("Список товаров не может быть пустым");
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

    private Map<UUID, WarehouseProduct> getWarehouseProductsById(List<UUID> productIds) {
        return warehouseProductRepository.findAllByProductIdIn(productIds).stream()
                .collect(Collectors.toMap(
                        WarehouseProduct::getProductId,
                        warehouseProduct -> warehouseProduct
                ));
    }

    private BookedProductsDto checkAndCalculateBookedProducts(
            Map<UUID, Long> products,
            Map<UUID, WarehouseProduct> warehouseProductsById,
            boolean decreaseQuantity
    ) {
        double deliveryWeight = 0.0;
        double deliveryVolume = 0.0;
        boolean fragile = false;

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
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
            if (decreaseQuantity) {
                warehouseProduct.setQuantity(availableQuantity - requiredQuantity);
            }
        }

        BookedProductsDto bookedProductsDto = new BookedProductsDto();
        bookedProductsDto.setDeliveryVolume(deliveryVolume);
        bookedProductsDto.setFragile(fragile);
        bookedProductsDto.setDeliveryWeight(deliveryWeight);
        return bookedProductsDto;
    }
}
