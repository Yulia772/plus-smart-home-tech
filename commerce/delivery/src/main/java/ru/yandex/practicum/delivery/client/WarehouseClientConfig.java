package ru.yandex.practicum.delivery.client;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class WarehouseClientConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new WarehouseErrorDecoder();
    }
}
