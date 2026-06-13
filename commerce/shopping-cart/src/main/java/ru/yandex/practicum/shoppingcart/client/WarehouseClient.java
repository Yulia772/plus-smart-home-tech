package ru.yandex.practicum.shoppingcart.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.interactionapi.warehouse.api.WarehouseApi;

@FeignClient(
        name = "warehouse",
        configuration = WarehouseClientConfig.class,
        fallback = WarehouseClientFallback.class
)
public interface WarehouseClient extends WarehouseApi {
}
