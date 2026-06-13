package ru.yandex.practicum.warehouse.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.interactionapi.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.warehouse.model.WarehouseProduct;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

    @Mapping(source = "dimension.width", target = "width")
    @Mapping(source = "dimension.height", target = "height")
    @Mapping(source = "dimension.depth", target = "depth")
    @Mapping(target = "quantity", ignore = true)
    WarehouseProduct toProduct(NewProductInWarehouseRequest request);
}
