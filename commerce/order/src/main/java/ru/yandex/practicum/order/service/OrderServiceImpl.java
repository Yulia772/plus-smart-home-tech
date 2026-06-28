package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interactionapi.order.CreateNewOrderRequest;
import ru.yandex.practicum.interactionapi.order.OrderDto;
import ru.yandex.practicum.interactionapi.order.OrderState;
import ru.yandex.practicum.interactionapi.warehouse.AddressDto;
import ru.yandex.practicum.order.exception.BadRequestException;
import ru.yandex.practicum.order.exception.NotFoundException;
import ru.yandex.practicum.order.mapper.AddressMapper;
import ru.yandex.practicum.order.mapper.OrderMapper;
import ru.yandex.practicum.order.model.AddressEntity;
import ru.yandex.practicum.order.model.OrderEntity;
import ru.yandex.practicum.order.repository.AddressRepository;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getClientOrders(String username, int page, int size) {
        if (username == null || username.isBlank()) {
            throw new BadRequestException("Передано неверное значение");
        }
        log.info("Получение заказов пользователя: username={}, page={}, size={}", username, page, size);
        int pageNumber = page;
        if (pageNumber < 0) {
            pageNumber = 0;
        }

        int pageSize = size;
        if (pageSize <= 0) {
            pageSize = 20;
        }
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<OrderEntity> orders = orderRepository.findByUsername(username, pageable);
        log.info("Найдено заказов пользователя {}: {}", username, orders.getNumberOfElements());

        return orders.stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest orderRequest) {
        if (orderRequest == null) {
            throw new BadRequestException("Передано неверное значение");
        }
        if (orderRequest.getUsername() == null || orderRequest.getUsername().isBlank()) {
            throw new BadRequestException("Передано неверное значение");
        }
        if (orderRequest.getShoppingCart() == null) {
            throw new BadRequestException("Передано неверное значение");
        }
        if (orderRequest.getShoppingCart().getShoppingCartId() == null) {
            throw new BadRequestException("Передано неверное значение");
        }
        Map<UUID, Long> cartProducts = orderRequest.getShoppingCart().getProducts();
        if (cartProducts == null || cartProducts.isEmpty()) {
            throw new BadRequestException("Передано неверное значение");
        }

        AddressDto deliveryAddress = orderRequest.getDeliveryAddress();
        validateAddress(deliveryAddress);

        for (Map.Entry<UUID, Long> entry : cartProducts.entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();

            if (productId == null) {
                throw new BadRequestException("Id товара не может быть пустым");
            }
            if (quantity == null || quantity <= 0) {
                throw new BadRequestException("Количество товара должно быть положительным");
            }
        }
        log.info("Создание нового заказа для пользователя: {}", orderRequest.getUsername());

        AddressEntity addressEntity = addressMapper.toEntity(deliveryAddress);
        addressEntity.setAddressId(UUID.randomUUID());
        AddressEntity savedAddress = addressRepository.save(addressEntity);

        OrderEntity orderEntity = createOrderEntity(orderRequest, savedAddress, cartProducts);
        OrderEntity savedOrder = orderRepository.save(orderEntity);
        log.info("Создан заказ: orderId={}, username={}", savedOrder.getOrderId(), savedOrder.getUsername());
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderEntity getOrderOrThrow(UUID orderId) {
        if (orderId == null) {
            throw new BadRequestException("Передано неверное значение");
        }
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.warn("Заказ не найден: orderId={}", orderId);
                    return new NotFoundException("Заказ с таким id не найден");
                });
    }

    @Override
    @Transactional
    public OrderEntity save(OrderEntity order) {
        if (order == null) {
            throw new BadRequestException("Передано неверное значение");
        }
        OrderEntity savedOrder = orderRepository.save(order);
        log.info("Заказ сохранен: orderId={}, state={}", savedOrder.getOrderId(), savedOrder.getState());
        return savedOrder;
    }

    private void validateAddress(AddressDto address) {
        if (address == null) {
            throw new BadRequestException("Адрес доставки не может быть пустым");
        }
        if (address.getCountry() == null || address.getCountry().isBlank()
                || address.getCity() == null || address.getCity().isBlank()
                || address.getStreet() == null || address.getStreet().isBlank()
                || address.getHouse() == null || address.getHouse().isBlank()) {
            throw new BadRequestException("Адрес заполнен некорректно");
        }
    }

    private OrderEntity createOrderEntity(CreateNewOrderRequest orderRequest,
                                          AddressEntity savedAddress,
                                          Map<UUID, Long> cartProducts) {
        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setOrderId(UUID.randomUUID());
        orderEntity.setUsername(orderRequest.getUsername());
        orderEntity.setShoppingCartId(orderRequest.getShoppingCart().getShoppingCartId());
        orderEntity.setProducts(new HashMap<>(cartProducts));
        orderEntity.setState(OrderState.NEW);
        orderEntity.setToAddress(savedAddress);

        return orderEntity;
    }
}

