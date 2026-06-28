package ru.yandex.practicum.order.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.interactionapi.order.OrderDto;
import ru.yandex.practicum.order.model.OrderEntity;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderDto toDto(OrderEntity orderEntity);
}
