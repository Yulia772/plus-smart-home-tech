package ru.yandex.practicum.analyzer.serialization;

import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import telemetry.serialization.BaseAvroDeserializer;

public class SensorsSnapshotDeserializer extends BaseAvroDeserializer<SensorsSnapshotAvro> {

    public SensorsSnapshotDeserializer() {
        super(SensorsSnapshotAvro.getClassSchema());
    }
}
