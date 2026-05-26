package ru.yandex.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;

@Component
public class HubEventMapper {

    public HubEventAvro toAvro(HubEventProto hubEvent) {
        Object payload;
        switch (hubEvent.getPayloadCase()) {
            case DEVICE_ADDED -> {
                DeviceAddedEventProto addedEvent = hubEvent.getDeviceAdded();

                payload = DeviceAddedEventAvro.newBuilder()
                        .setId(addedEvent.getId())
                        .setType(DeviceTypeAvro.valueOf(addedEvent.getType().name()))
                        .build();
            }
            case DEVICE_REMOVED -> {
                DeviceRemovedEventProto removedEvent = hubEvent.getDeviceRemoved();

                payload = DeviceRemovedEventAvro.newBuilder()
                        .setId(removedEvent.getId())
                        .build();
            }
            case SCENARIO_ADDED -> {
                ScenarioAddedEventProto scenarioAddedEvent = hubEvent.getScenarioAdded();
                payload = ScenarioAddedEventAvro.newBuilder()
                        .setActions(scenarioAddedEvent.getActionList().stream()
                                .map(this::mapAction)
                                .toList())
                        .setConditions(scenarioAddedEvent.getConditionList().stream()
                                .map(this::mapCondition)
                                .toList())
                        .setName(scenarioAddedEvent.getName())
                        .build();
            }
            case SCENARIO_REMOVED -> {
                ScenarioRemovedEventProto scenarioRemovedEvent = hubEvent.getScenarioRemoved();

                payload = ScenarioRemovedEventAvro.newBuilder()
                        .setName(scenarioRemovedEvent.getName())
                        .build();
            }
            default -> throw new IllegalArgumentException("Неизвестный тип события хаба: " + hubEvent.getClass());
        }
        return HubEventAvro.newBuilder()
                .setHubId(hubEvent.getHubId())
                .setPayload(payload)
                .setTimestamp(Instant.ofEpochSecond(
                        hubEvent.getTimestamp().getSeconds(),
                        hubEvent.getTimestamp().getNanos()))
                .build();
    }

    private ScenarioConditionAvro mapCondition(ScenarioConditionProto condition) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setValue(mapConditionValue(condition))
                .build();
    }

    private DeviceActionAvro mapAction(DeviceActionProto action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()))
                .setValue(action.hasValue() ? action.getValue() : null)
                .build();
    }

    private Object mapConditionValue(ScenarioConditionProto conditionProto) {
        return switch (conditionProto.getValueCase()) {
            case BOOL_VALUE -> conditionProto.getBoolValue();
            case INT_VALUE -> conditionProto.getIntValue();
            case VALUE_NOT_SET -> null;
        };
    }
}
