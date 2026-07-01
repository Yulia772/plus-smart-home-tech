package ru.yandex.practicum.payment.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.interactionapi.payment.PaymentDto;
import ru.yandex.practicum.payment.model.PaymentEntity;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentDto toDto(PaymentEntity payment);
}
