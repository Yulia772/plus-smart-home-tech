package ru.yandex.practicum.payment.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.interactionapi.exception.CommonExceptionHandler;

@RestControllerAdvice
public class PaymentExceptionHandler extends CommonExceptionHandler {
}
