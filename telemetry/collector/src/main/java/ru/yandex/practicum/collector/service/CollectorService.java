package ru.yandex.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.config.CollectorKafkaConfig;
import ru.yandex.practicum.collector.mapper.HubEventMapper;
import ru.yandex.practicum.collector.mapper.SensorEventMapper;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorService {

    private final SensorEventMapper sensorEventMapper;
    private final HubEventMapper hubEventMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CollectorKafkaConfig kafkaConfig;

    public void collectSensorEvent(SensorEventProto sensorEvent) {
        SensorEventAvro sensorEventAvro = sensorEventMapper.toAvro(sensorEvent);
        String sensorTopic = kafkaConfig.getTopic().getSensors();

        kafkaTemplate.send(
                sensorTopic,
                sensorEvent.getHubId(),
                sensorEventAvro
        );
        log.info("Отправлено событие датчика в Kafka: hubId = {}, id = {}", sensorEvent.getHubId(), sensorEvent.getId());
    }

    public void collectHubEvent(HubEventProto hubEvent) {
        HubEventAvro hubEventAvro = hubEventMapper.toAvro(hubEvent);
        String hubTopic = kafkaConfig.getTopic().getHubs();

        kafkaTemplate.send(
                hubTopic,
                hubEvent.getHubId(),
                hubEventAvro
        );
        log.info("Отправлено событие хаба в Kafka: hubId = {}, type = {}", hubEvent.getHubId(), hubEvent.getPayloadCase());
    }
}
