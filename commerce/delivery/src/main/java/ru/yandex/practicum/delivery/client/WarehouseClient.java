package ru.yandex.practicum.delivery.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.interactionapi.warehouse.api.WarehouseApi;

@FeignClient(
        name = "warehouse",
        configuration = WarehouseClientConfig.class
)
public interface WarehouseClient extends WarehouseApi {
}
