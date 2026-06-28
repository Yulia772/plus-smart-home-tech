package ru.yandex.practicum.interactionapi.delivery;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.interactionapi.warehouse.AddressDto;

import java.util.UUID;

@Getter
@Setter
@ToString
public class DeliveryDto {
    @NotNull
    private UUID deliveryId;
    @NotNull
    private DeliveryState deliveryState;
    @NotNull
    private AddressDto fromAddress;
    @NotNull
    private UUID orderId;
    @NotNull
    private AddressDto toAddress;
}
