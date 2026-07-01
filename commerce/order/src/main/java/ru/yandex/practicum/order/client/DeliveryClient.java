package ru.yandex.practicum.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.interactionapi.delivery.api.DeliveryApi;

@FeignClient(
        name = "delivery",
        configuration = FeignClientConfig.class
)
public interface DeliveryClient extends DeliveryApi {
}
