package ru.yandex.practicum.delivery.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.delivery.model.DeliveryAddress;
import ru.yandex.practicum.delivery.model.DeliveryEntity;
import ru.yandex.practicum.interactionapi.delivery.DeliveryDto;
import ru.yandex.practicum.interactionapi.warehouse.AddressDto;

@Mapper(componentModel = "spring")
public interface DeliveryMapper {
    DeliveryDto toDto(DeliveryEntity delivery);

    AddressDto toDto(DeliveryAddress address);

    DeliveryAddress toEntity(AddressDto dto);
}
