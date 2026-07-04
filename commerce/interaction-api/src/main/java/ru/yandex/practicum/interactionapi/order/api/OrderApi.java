package ru.yandex.practicum.interactionapi.order.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interactionapi.order.CreateNewOrderRequest;
import ru.yandex.practicum.interactionapi.order.OrderDto;
import ru.yandex.practicum.interactionapi.order.ProductReturnRequest;

import java.util.List;
import java.util.UUID;

public interface OrderApi {

    @GetMapping("/api/v1/order")
    List<OrderDto> getClientOrders(@RequestParam("username") String username,
                                   @RequestParam(required = false, defaultValue = "0") int page,
                                   @RequestParam(required = false, defaultValue = "20") int size);

    @PutMapping("/api/v1/order")
    OrderDto createNewOrder(@RequestBody @Valid CreateNewOrderRequest orderRequest);

    @PostMapping("/api/v1/order/return")
    OrderDto productReturn(@RequestBody @Valid ProductReturnRequest request);

    @PostMapping("/api/v1/order/payment")
    OrderDto payment(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/payment/successful")
    OrderDto paymentSuccessful(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/payment/failed")
    OrderDto paymentFailed(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/delivery")
    OrderDto delivery(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/delivery/failed")
    OrderDto deliveryFailed(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/completed")
    OrderDto complete(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/calculate/total")
    OrderDto calculateTotalCost(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/calculate/delivery")
    OrderDto calculateDeliveryCost(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/assembly")
    OrderDto assembly(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/assembly/failed")
    OrderDto assemblyFailed(@RequestBody UUID orderId);
}
