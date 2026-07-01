package ru.yandex.practicum.payment.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import ru.yandex.practicum.interactionapi.exception.BadRequestException;
import ru.yandex.practicum.interactionapi.exception.NotFoundException;

public class OrderErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 400) {
            return new BadRequestException("Сервис заказов не смог обработать запрос" + methodKey);
        }
        if (response.status() == 404) {
            return new NotFoundException("Сервис заказов не нашел данные для запроса:" + methodKey);
        }
        return defaultDecoder.decode(methodKey, response);
    }
}
