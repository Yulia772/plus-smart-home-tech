package ru.yandex.practicum.shoppingcart.client;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class WarehouseClientConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new WarehouseErrorDecoder();
    }
}
