package ru.yandex.practicum.delivery.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.interactionapi.delivery.DeliveryState;

import java.util.UUID;

@Entity
@Table(name = "deliveries", schema = "delivery")
@Getter
@Setter
@ToString
public class DeliveryEntity {
    @Id
    @Column(name = "delivery_id", nullable = false)
    private UUID deliveryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_state", nullable = false)
    private DeliveryState deliveryState;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "from_country", nullable = false)),
            @AttributeOverride(name = "city", column = @Column(name = "from_city", nullable = false)),
            @AttributeOverride(name = "street", column = @Column(name = "from_street", nullable = false)),
            @AttributeOverride(name = "house", column = @Column(name = "from_house", nullable = false)),
            @AttributeOverride(name = "flat", column = @Column(name = "from_flat"))
    })
    private DeliveryAddress fromAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "to_country", nullable = false)),
            @AttributeOverride(name = "city", column = @Column(name = "to_city", nullable = false)),
            @AttributeOverride(name = "street", column = @Column(name = "to_street", nullable = false)),
            @AttributeOverride(name = "house", column = @Column(name = "to_house", nullable = false)),
            @AttributeOverride(name = "flat", column = @Column(name = "to_flat"))
    })
    private DeliveryAddress toAddress;

    @Column(name = "delivery_weight")
    private Double deliveryWeight;

    @Column(name = "delivery_volume")
    private Double deliveryVolume;

    @Column(name = "fragile")
    private Boolean fragile;
}
