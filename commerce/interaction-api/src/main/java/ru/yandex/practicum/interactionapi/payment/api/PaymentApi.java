package ru.yandex.practicum.interactionapi.payment.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.interactionapi.order.OrderDto;
import ru.yandex.practicum.interactionapi.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentApi {
    @PostMapping("/api/v1/payment")
    PaymentDto payment(@RequestBody @Valid OrderDto dto);

    @PostMapping("/api/v1/payment/totalCost")
    BigDecimal getTotalCost(@RequestBody @Valid OrderDto dto);

    @PostMapping("/api/v1/payment/refund")
    void paymentSuccess(@RequestBody UUID paymentId);

    @PostMapping("/api/v1/payment/productCost")
    BigDecimal productCost(@RequestBody @Valid OrderDto dto);

    @PostMapping("/api/v1/payment/failed")
    void paymentFailed(@RequestBody UUID paymentId);
}
