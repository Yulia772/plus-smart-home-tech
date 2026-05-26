package ru.yandex.practicum.aggregator.serialization;

import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import telemetry.serialization.BaseAvroDeserializer;

public class SensorEventDeserializer extends BaseAvroDeserializer<SensorEventAvro> {
    public SensorEventDeserializer() {
        super(SensorEventAvro.getClassSchema());
    }
}