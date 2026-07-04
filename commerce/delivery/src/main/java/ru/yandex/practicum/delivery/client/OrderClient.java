package ru.yandex.practicum.delivery.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.interactionapi.order.api.OrderApi;

@FeignClient(
        name = "order",
        configuration = FeignClientConfig.class
)
public interface OrderClient extends OrderApi {
}
