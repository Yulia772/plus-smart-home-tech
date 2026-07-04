package ru.yandex.practicum.shoppingcart.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.interactionapi.exception.CommonExceptionHandler;

@RestControllerAdvice
public class ShoppingCartExceptionHandler extends CommonExceptionHandler {
}
