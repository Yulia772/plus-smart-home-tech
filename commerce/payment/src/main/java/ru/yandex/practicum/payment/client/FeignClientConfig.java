package ru.yandex.practicum.payment.client;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import ru.yandex.practicum.interactionapi.client.CommonFeignErrorDecoder;

public class FeignClientConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CommonFeignErrorDecoder();
    }
}
