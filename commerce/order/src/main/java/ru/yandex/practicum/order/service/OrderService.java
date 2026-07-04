package ru.yandex.practicum.order.service;

import ru.yandex.practicum.interactionapi.order.CreateNewOrderRequest;
import ru.yandex.practicum.interactionapi.order.OrderDto;
import ru.yandex.practicum.order.model.OrderEntity;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    List<OrderDto> getClientOrders(String username, int page, int size);

    OrderDto createNewOrder(CreateNewOrderRequest orderRequest);

    OrderEntity getOrderOrThrow(UUID orderId);

    OrderEntity save(OrderEntity order);
}
