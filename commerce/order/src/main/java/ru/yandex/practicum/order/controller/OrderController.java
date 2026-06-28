package ru.yandex.practicum.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.interactionapi.order.CreateNewOrderRequest;
import ru.yandex.practicum.interactionapi.order.OrderDto;
import ru.yandex.practicum.interactionapi.order.ProductReturnRequest;
import ru.yandex.practicum.interactionapi.order.api.OrderApi;
import ru.yandex.practicum.order.service.OrderService;
import ru.yandex.practicum.order.workflow.OrderWorkflowService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {
    private final OrderService orderService;
    private final OrderWorkflowService orderWorkflowService;

    @Override
    public List<OrderDto> getClientOrders(String username, int page, int size) {
        return orderService.getClientOrders(username, page, size);
    }

    @Override
    public OrderDto createNewOrder(CreateNewOrderRequest orderRequest) {
        return orderService.createNewOrder(orderRequest);
    }

    @Override
    public OrderDto productReturn(ProductReturnRequest request) {
        return orderWorkflowService.productReturn(request);
    }

    @Override
    public OrderDto payment(UUID orderId) {
        return orderWorkflowService.payment(orderId);
    }

    @Override
    public OrderDto paymentSuccessful(UUID orderId) {
        return orderWorkflowService.paymentSuccessful(orderId);
    }

    @Override
    public OrderDto paymentFailed(UUID orderId) {
        return orderWorkflowService.paymentFailed(orderId);
    }

    @Override
    public OrderDto delivery(UUID orderId) {
        return orderWorkflowService.delivery(orderId);
    }

    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        return orderWorkflowService.deliveryFailed(orderId);
    }

    @Override
    public OrderDto complete(UUID orderId) {
        return orderWorkflowService.complete(orderId);
    }

    @Override
    public OrderDto calculateTotalCost(UUID orderId) {
        return orderWorkflowService.calculateTotalCost(orderId);
    }

    @Override
    public OrderDto calculateDeliveryCost(UUID orderId) {
        return orderWorkflowService.calculateDeliveryCost(orderId);
    }

    @Override
    public OrderDto assembly(UUID orderId) {
        return orderWorkflowService.assembly(orderId);
    }

    @Override
    public OrderDto assemblyFailed(UUID orderId) {
        return orderWorkflowService.assemblyFailed(orderId);
    }
}
