package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@Table(name = "sensors")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Sensor {

    @Id
    private String id;

    @Column(name = "hub_id")
    private String hubId;
}
