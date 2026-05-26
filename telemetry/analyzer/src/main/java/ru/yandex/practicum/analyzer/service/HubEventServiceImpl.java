package ru.yandex.practicum.analyzer.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Slf4j
@Service
@AllArgsConstructor
public class HubEventServiceImpl implements HubEventService {
    private final SensorEventService sensorEventService;
    private final ScenarioEventService scenarioEventService;

    @Override
    @Transactional
    public void handle(HubEventAvro eventAvro) {
        String hubId = eventAvro.getHubId();

        if (eventAvro.getPayload() instanceof DeviceAddedEventAvro payload) {
            sensorEventService.handleDeviceAdded(hubId, payload);
        } else if (eventAvro.getPayload() instanceof DeviceRemovedEventAvro payload) {
            sensorEventService.handleDeviceRemoved(hubId, payload);
        } else if (eventAvro.getPayload() instanceof ScenarioAddedEventAvro payload) {
            scenarioEventService.handleScenarioAdded(hubId, payload);
        } else if (eventAvro.getPayload() instanceof ScenarioRemovedEventAvro payload) {
            scenarioEventService.handleScenarioRemoved(hubId, payload);
        } else {
            log.warn("Пришел неизвестный payload = {}",
                    eventAvro.getPayload());
        }
    }
}



