package ru.yandex.practicum.order.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "addresses", schema = "orders")
@Getter
@Setter
public class AddressEntity {
    @Id
    @Column(name = "address_id")
    private UUID addressId;
    @Column(nullable = false)
    private String country;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String street;
    @Column(nullable = false)
    private String house;
    private String flat;
}
