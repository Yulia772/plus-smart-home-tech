package ru.yandex.practicum.delivery.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Getter
@Setter
@ToString
public class DeliveryAddress {
    private String country;
    private String city;
    private String street;
    private String house;
    private String flat;
}
