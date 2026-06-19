package ru.yandex.practicum.interactionapi.warehouse;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AddressDto {
    private String country;
    private String city;
    private String street;
    private String house;
    private String flat;
}
