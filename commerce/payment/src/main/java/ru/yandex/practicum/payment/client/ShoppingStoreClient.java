package ru.yandex.practicum.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.interactionapi.store.api.ShoppingStoreApi;

@FeignClient(
        name = "shopping-store",
        configuration = FeignClientConfig.class
)
public interface ShoppingStoreClient extends ShoppingStoreApi {
}
