package ru.yandex.practicum.order.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import ru.yandex.practicum.order.exception.BadRequestException;

public class PaymentErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 400) {
            return new BadRequestException("Сервис оплаты не смог обработать запрос" + methodKey);
        }
        return defaultDecoder.decode(methodKey, response);
    }
}
