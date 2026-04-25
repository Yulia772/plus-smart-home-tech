package ru.yandex.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.model.sensor.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Component
public class SensorEventMapper {

    public SensorEventAvro toAvro(SensorEvent sensorEvent) {
        Object payload;
        switch (sensorEvent) {
            case LightSensorEvent lightSensorEvent -> {
                payload = LightSensorAvro.newBuilder()
                        .setLinkQuality(lightSensorEvent.getLinkQuality())
                        .setLuminosity(lightSensorEvent.getLuminosity())
                        .build();
            }
            case ClimateSensorEvent climateSensorEvent -> {
                payload = ClimateSensorAvro.newBuilder()
                        .setCo2Level(climateSensorEvent.getCo2Level())
                        .setHumidity(climateSensorEvent.getHumidity())
                        .setTemperatureC(climateSensorEvent.getTemperatureC())
                        .build();
            }
            case MotionSensorEvent motionSensorEvent -> {
                payload = MotionSensorAvro.newBuilder()
                        .setLinkQuality(motionSensorEvent.getLinkQuality())
                        .setMotion(motionSensorEvent.isMotion())
                        .setVoltage(motionSensorEvent.getVoltage())
                        .build();
            }
            case SwitchSensorEvent switchSensorEvent -> {
                payload = SwitchSensorAvro.newBuilder()
                        .setState(switchSensorEvent.isState())
                        .build();
            }
            case TemperatureSensorEvent temperatureSensorEvent -> {
                payload = TemperatureSensorAvro.newBuilder()
                        .setId(temperatureSensorEvent.getId())
                        .setHubId(temperatureSensorEvent.getHubId())
                        .setTimestamp(temperatureSensorEvent.getTimestamp())
                        .setTemperatureC(temperatureSensorEvent.getTemperatureC())
                        .setTemperatureF(temperatureSensorEvent.getTemperatureF())
                        .build();
            }
            default -> throw new IllegalArgumentException("Неизвестный тип события сенсора: " + sensorEvent.getClass());
        }
        return SensorEventAvro.newBuilder()
                .setId(sensorEvent.getId())
                .setHubId(sensorEvent.getHubId())
                .setTimestamp(sensorEvent.getTimestamp())
                .setPayload(payload)
                .build();
    }
}
