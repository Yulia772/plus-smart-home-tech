package ru.yandex.practicum.interactionapi.warehouse;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BookedProductsDto {
    @NotNull
    private Double deliveryWeight;
    @NotNull
    private Double deliveryVolume;
    @NotNull
    private Boolean fragile;
}
