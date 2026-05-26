package ru.yandex.practicum.analyzer.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.model.Sensor;
import ru.yandex.practicum.analyzer.repository.ScenarioActionRepository;
import ru.yandex.practicum.analyzer.repository.ScenarioConditionRepository;
import ru.yandex.practicum.analyzer.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Service
public class SensorEventService {
    private final SensorRepository sensorRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;

    public void handleDeviceAdded(String hubId, DeviceAddedEventAvro event) {
        if (sensorRepository.findByIdAndHubId(event.getId(), hubId).isPresent()) {
            log.warn("Такой датчик id = {}, hubId = {} уже зарегистрирован", event.getId(), hubId);
            return;
        }
        Sensor sensor = Sensor.builder()
                .id(event.getId())
                .hubId(hubId)
                .build();
        sensorRepository.save(sensor);
        log.info("Датчик с id = {}, hubId = {} сохранен", sensor.getId(), hubId);
    }

    public void handleDeviceRemoved(String hubId, DeviceRemovedEventAvro event) {
        Optional<Sensor> sensorOptional = sensorRepository.findByIdAndHubId(event.getId(), hubId);

        if (sensorOptional.isEmpty()) {
            log.warn("Датчик с id = {}, hubId = {} не найден", event.getId(), hubId);
            return;
        }
        Sensor sensor = sensorOptional.get();

        scenarioConditionRepository.deleteAllBySensorId(sensor.getId());
        scenarioActionRepository.deleteAllBySensorId(sensor.getId());
        sensorRepository.delete(sensor);

        log.info("Датчик с id = {}, hubId = {} удален", sensor.getId(), hubId);
    }
}
