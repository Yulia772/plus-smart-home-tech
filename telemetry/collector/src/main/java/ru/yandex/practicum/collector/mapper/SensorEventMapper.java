package ru.yandex.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;

@Component
public class SensorEventMapper {

    public SensorEventAvro toAvro(SensorEventProto sensorEvent) {
        Object payload;
        switch (sensorEvent.getPayloadCase()) {
            case LIGHT_SENSOR -> {
                LightSensorProto lightSensorEvent = sensorEvent.getLightSensor();

                payload = LightSensorAvro.newBuilder()
                        .setLinkQuality(lightSensorEvent.getLinkQuality())
                        .setLuminosity(lightSensorEvent.getLuminosity())
                        .build();
            }
            case CLIMATE_SENSOR -> {
                ClimateSensorProto climateSensorEvent = sensorEvent.getClimateSensor();

                payload = ClimateSensorAvro.newBuilder()
                        .setCo2Level(climateSensorEvent.getCo2Level())
                        .setHumidity(climateSensorEvent.getHumidity())
                        .setTemperatureC(climateSensorEvent.getTemperatureC())
                        .build();
            }
            case MOTION_SENSOR -> {
                MotionSensorProto motionSensorEvent = sensorEvent.getMotionSensor();

                payload = MotionSensorAvro.newBuilder()
                        .setLinkQuality(motionSensorEvent.getLinkQuality())
                        .setMotion(motionSensorEvent.getMotion())
                        .setVoltage(motionSensorEvent.getVoltage())
                        .build();
            }
            case SWITCH_SENSOR -> {
                SwitchSensorProto switchSensorEvent = sensorEvent.getSwitchSensor();

                payload = SwitchSensorAvro.newBuilder()
                        .setState(switchSensorEvent.getState())
                        .build();
            }
            case TEMPERATURE_SENSOR -> {
                TemperatureSensorProto temperatureSensorEvent = sensorEvent.getTemperatureSensor();

                payload = TemperatureSensorAvro.newBuilder()
                        .setTemperatureC(temperatureSensorEvent.getTemperatureC())
                        .setTemperatureF(temperatureSensorEvent.getTemperatureF())
                        .build();
            }
            default -> throw new IllegalArgumentException("Неизвестный тип события сенсора: " + sensorEvent.getPayloadCase());
        }
        return SensorEventAvro.newBuilder()
                .setId(sensorEvent.getId())
                .setHubId(sensorEvent.getHubId())
                .setTimestamp(Instant.ofEpochSecond(
                        sensorEvent.getTimestamp().getSeconds(),
                        sensorEvent.getTimestamp().getNanos()))
                .setPayload(payload)
                .build();
    }
}
