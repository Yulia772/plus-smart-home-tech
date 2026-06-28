package ru.yandex.practicum.order.client;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class PaymentClientConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new PaymentErrorDecoder();
    }
}
