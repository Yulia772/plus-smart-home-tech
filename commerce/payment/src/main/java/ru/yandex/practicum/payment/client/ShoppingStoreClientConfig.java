package ru.yandex.practicum.payment.client;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class ShoppingStoreClientConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new ShoppingStoreErrorDecoder();
    }
}
