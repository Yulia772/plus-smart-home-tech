package ru.yandex.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.model.hub.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Component
public class HubEventMapper {

    public HubEventAvro toAvro(HubEvent hubEvent) {
        Object payload;
        switch (hubEvent) {
            case DeviceAddedEvent addedEvent -> {
                payload = DeviceAddedEventAvro.newBuilder()
                        .setId(addedEvent.getId())
                        .setType(DeviceTypeAvro.valueOf(addedEvent.getDeviceType().name()))
                        .build();
            }
            case DeviceRemovedEvent removedEvent -> {
                payload = DeviceRemovedEventAvro.newBuilder()
                        .setId(removedEvent.getId())
                        .build();
            }
            case ScenarioAddedEvent scenarioAddedEvent -> {
                payload = ScenarioAddedEventAvro.newBuilder()
                        .setActions(scenarioAddedEvent.getActions().stream()
                                .map(this::mapAction)
                                .toList())
                        .setConditions(scenarioAddedEvent.getConditions().stream()
                                .map(this::mapCondition)
                                .toList())
                        .setName(scenarioAddedEvent.getName())
                        .build();
            }
            case ScenarioRemovedEvent scenarioRemovedEvent -> {
                payload = ScenarioRemovedEventAvro.newBuilder()
                        .setName(scenarioRemovedEvent.getName())
                        .build();
            }
            default -> throw new IllegalArgumentException("Неизвестный тип события хаба: " + hubEvent.getClass());
        }
        return HubEventAvro.newBuilder()
                .setHubId(hubEvent.getHubId())
                .setPayload(payload)
                .setTimestamp(hubEvent.getTimestamp())
                .build();
    }

    private ScenarioConditionAvro mapCondition(ScenarioCondition condition) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setValue(condition.getValue())
                .build();
    }

    private DeviceActionAvro mapAction(DeviceAction action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()))
                .setValue(action.getValue())
                .build();
    }
}
