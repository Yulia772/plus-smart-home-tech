package ru.yandex.practicum.order.workflow;

import ru.yandex.practicum.interactionapi.order.OrderDto;
import ru.yandex.practicum.interactionapi.order.ProductReturnRequest;

import java.util.UUID;

public interface OrderWorkflowService {
    OrderDto productReturn(ProductReturnRequest request);

    OrderDto payment(UUID orderId);

    OrderDto paymentSuccessful(UUID orderId);

    OrderDto paymentFailed(UUID orderId);

    OrderDto delivery(UUID orderId);

    OrderDto deliveryFailed(UUID orderId);

    OrderDto complete(UUID orderId);

    OrderDto calculateTotalCost(UUID orderId);

    OrderDto calculateDeliveryCost(UUID orderId);

    OrderDto assembly(UUID orderId);

    OrderDto assemblyFailed(UUID orderId);
}


