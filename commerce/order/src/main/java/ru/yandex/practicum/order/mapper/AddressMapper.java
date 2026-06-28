package ru.yandex.practicum.order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.interactionapi.warehouse.AddressDto;
import ru.yandex.practicum.order.model.AddressEntity;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    @Mapping(target = "addressId", ignore = true)
    AddressEntity toEntity(AddressDto dto);

    AddressDto toDto(AddressEntity entity);
}
