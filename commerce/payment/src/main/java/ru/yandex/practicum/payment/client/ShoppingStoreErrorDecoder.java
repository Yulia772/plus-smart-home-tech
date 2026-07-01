package ru.yandex.practicum.payment.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import ru.yandex.practicum.interactionapi.exception.BadRequestException;
import ru.yandex.practicum.interactionapi.exception.NotFoundException;

public class ShoppingStoreErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 400) {
            return new BadRequestException("Сервис витрины не смог обработать запрос" + methodKey);
        }
        if (response.status() == 404) {
            return new NotFoundException("Сервис витрины не нашел данные для запроса:" + methodKey);
        }
        return defaultDecoder.decode(methodKey, response);
    }
}
