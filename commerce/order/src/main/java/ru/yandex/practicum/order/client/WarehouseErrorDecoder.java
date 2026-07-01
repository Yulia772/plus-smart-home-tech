package ru.yandex.practicum.order.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import ru.yandex.practicum.interactionapi.exception.BadRequestException;
import ru.yandex.practicum.interactionapi.exception.NotFoundException;

public class WarehouseErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 400) {
            return new BadRequestException("Склад не подтвердил наличие товаров" + methodKey);
        }
        if (response.status() == 404) {
            return new NotFoundException("Сервис склада не нашел данные для запроса:" + methodKey);
        }
        return defaultDecoder.decode(methodKey, response);
    }
}