package ru.yandex.practicum.order.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.interactionapi.exception.CommonExceptionHandler;

@RestControllerAdvice
public class OrderExceptionHandler extends CommonExceptionHandler {
}
