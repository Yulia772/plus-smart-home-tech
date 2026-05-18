package ru.yandex.practicum.analyzer.serialization;

import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import telemetry.serialization.BaseAvroDeserializer;

public class HubEventDeserializer extends BaseAvroDeserializer<HubEventAvro> {

    public HubEventDeserializer() {
        super(HubEventAvro.getClassSchema());
    }
}
