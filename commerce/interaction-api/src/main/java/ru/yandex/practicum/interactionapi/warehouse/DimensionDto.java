package ru.yandex.practicum.interactionapi.warehouse;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DimensionDto {
    @NotNull
    @Positive
    private Double depth;
    @NotNull
    @Positive
    private Double height;
    @NotNull
    @Positive
    private Double width;
}
