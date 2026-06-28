package ru.yandex.practicum.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.interactionapi.payment.api.PaymentApi;

@FeignClient(
        name = "payment",
        configuration = PaymentClientConfig.class
)
public interface PaymentClient extends PaymentApi {
}
