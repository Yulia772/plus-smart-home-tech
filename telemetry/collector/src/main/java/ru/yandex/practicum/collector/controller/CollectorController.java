/*package ru.yandex.practicum.collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.collector.model.hub.HubEvent;
import ru.yandex.practicum.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.collector.service.CollectorService;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CollectorController {
    private final CollectorService collectorService;

    @PostMapping("/sensors")
    public void collectSensorEvent(
            @Valid @RequestBody SensorEvent sensorEvent
    ) {
        log.info("Получено событие датчика: {}", sensorEvent);
        collectorService.collectSensorEvent(sensorEvent);
    }

    @PostMapping("/hubs")
    public void collectHubEvent(
            @Valid @RequestBody HubEvent hubEvent
    ) {
        log.info("Получено событие хаба: {}", hubEvent);
        collectorService.collectHubEvent(hubEvent);
    }
}

 */
