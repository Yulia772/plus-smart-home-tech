package ru.yandex.practicum.shoppingstore.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.interactionapi.store.ProductDto;
import ru.yandex.practicum.shoppingstore.model.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDto toDto(Product product);

    Product toProduct(ProductDto productDto);
}
