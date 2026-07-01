package ru.yandex.practicum.order.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import ru.yandex.practicum.interactionapi.exception.BadRequestException;
import ru.yandex.practicum.interactionapi.exception.NotFoundException;

public class PaymentErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 400) {
            return new BadRequestException("Сервис оплаты не смог обработать запрос" + methodKey);
        }
        if (response.status() == 404) {
            return new NotFoundException("Сервис оплаты не нашел данные для запроса:" + methodKey);
        }
        return defaultDecoder.decode(methodKey, response);
    }
}
