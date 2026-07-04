package ru.yandex.practicum.payment.service;

import ru.yandex.practicum.interactionapi.order.OrderDto;
import ru.yandex.practicum.interactionapi.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {

    PaymentDto payment(OrderDto dto);

    BigDecimal getTotalCost(OrderDto dto);

    void paymentSuccess(UUID paymentId);

    BigDecimal productCost(OrderDto dto);

    void paymentFailed(UUID paymentId);
}
