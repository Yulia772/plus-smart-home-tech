package ru.yandex.practicum.interactionapi.warehouse;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class NewProductInWarehouseRequest {
    @NotNull
    private UUID productId;
    private Boolean fragile;
    @NotNull
    private DimensionDto dimension;
    @NotNull
    @Positive
    private Double weight;
}
