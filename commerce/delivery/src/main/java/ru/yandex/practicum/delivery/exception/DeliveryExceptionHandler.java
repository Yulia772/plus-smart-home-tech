package ru.yandex.practicum.delivery.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.interactionapi.exception.CommonExceptionHandler;

@RestControllerAdvice
public class DeliveryExceptionHandler extends CommonExceptionHandler {
}
