package ru.yandex.practicum.warehouse.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.interactionapi.exception.CommonExceptionHandler;

@RestControllerAdvice
public class WarehouseExceptionHandler extends CommonExceptionHandler {
}
