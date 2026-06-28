package ru.yandex.practicum.interactionapi.warehouse;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class ShippedToDeliveryRequest {
    @NotNull
    private UUID deliveryId;

    @NotNull
    private UUID orderId;
}
