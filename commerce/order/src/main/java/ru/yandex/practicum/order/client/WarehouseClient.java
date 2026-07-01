package ru.yandex.practicum.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.interactionapi.warehouse.api.WarehouseApi;

@FeignClient(
        name = "warehouse",
        configuration = FeignClientConfig.class
)
public interface WarehouseClient extends WarehouseApi {
}
