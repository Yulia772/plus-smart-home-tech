package ru.yandex.practicum.interactionapi.order.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interactionapi.order.CreateNewOrderRequest;
import ru.yandex.practicum.interactionapi.order.OrderDto;
import ru.yandex.practicum.interactionapi.order.ProductReturnRequest;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1/order")
public interface OrderApi {

    @GetMapping
    List<OrderDto> getClientOrders(@RequestParam("username") String username,
                                   @RequestParam(required = false, defaultValue = "0") int page,
                                   @RequestParam(required = false, defaultValue = "20") int size);

    @PutMapping
    OrderDto createNewOrder(@RequestBody @Valid CreateNewOrderRequest orderRequest);

    @PostMapping("/return")
    OrderDto productReturn(@RequestBody @Valid ProductReturnRequest request);

    @PostMapping("/payment")
    OrderDto payment(@RequestBody UUID orderId);

    @PostMapping("/payment/successful")
    OrderDto paymentSuccessful(@RequestBody UUID orderId);

    @PostMapping("/payment/failed")
    OrderDto paymentFailed(@RequestBody UUID orderId);

    @PostMapping("/delivery")
    OrderDto delivery(@RequestBody UUID orderId);

    @PostMapping("/delivery/failed")
    OrderDto deliveryFailed(@RequestBody UUID orderId);

    @PostMapping("/completed")
    OrderDto complete(@RequestBody UUID orderId);

    @PostMapping("/calculate/total")
    OrderDto calculateTotalCost(@RequestBody UUID orderId);

    @PostMapping("/calculate/delivery")
    OrderDto calculateDeliveryCost(@RequestBody UUID orderId);

    @PostMapping("/assembly")
    OrderDto assembly(@RequestBody UUID orderId);

    @PostMapping("/assembly/failed")
    OrderDto assemblyFailed(@RequestBody UUID orderId);
}
