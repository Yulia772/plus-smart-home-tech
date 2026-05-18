package ru.yandex.practicum.analyzer.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.model.Condition;
import ru.yandex.practicum.analyzer.model.ScenarioCondition;
import ru.yandex.practicum.analyzer.model.enums.ConditionOperation;
import ru.yandex.practicum.analyzer.model.enums.ConditionType;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.List;
import java.util.Map;

@Service
public class ConditionEvaluatorImpl implements ConditionEvaluator {
    @Override
    public boolean matches(List<ScenarioCondition> conditions, Map<String, SensorStateAvro> sensorsState) {
        for (ScenarioCondition condition : conditions) {
            String sensorId = condition.getSensor().getId();
            Condition scenarioCondition = condition.getCondition();

            SensorStateAvro sensorState = sensorsState.get(sensorId);
            if (sensorState == null) {
                return false;
            }
            Object data = sensorState.getData();

            Integer actualValue = extractActualValue(scenarioCondition.getType(), data);
            if (actualValue == null) {
                return false;
            }

            if (!compare(actualValue, scenarioCondition.getValue(), scenarioCondition.getOperation())) {
                return false;
            }
        }
        return true;
    }

    private boolean compare(Integer actualValue, Integer expectedValue, ConditionOperation operation) {
        if (actualValue == null || expectedValue == null || operation == null) {
            return false;
        }

        return switch (operation) {
            case EQUALS -> actualValue.equals(expectedValue);
            case GREATER_THAN -> actualValue > expectedValue;
            case LOWER_THAN -> actualValue < expectedValue;
        };
    }

    private Integer extractActualValue(ConditionType type, Object data) {
        switch (type) {
            case TEMPERATURE -> {
                if (data instanceof TemperatureSensorAvro payload) {
                    return payload.getTemperatureC();
                }
                if (data instanceof ClimateSensorAvro payload) {
                    return payload.getTemperatureC();
                }
                return null;
            }
            case MOTION -> {
                if (data instanceof MotionSensorAvro payload) {
                    return payload.getMotion() ? 1 : 0;
                }
                return null;
            }
            case SWITCH -> {
                if (data instanceof SwitchSensorAvro payload) {
                    return payload.getState() ? 1 : 0;
                }
                return null;
            }
            case CO2LEVEL -> {
                if (data instanceof ClimateSensorAvro payload) {
                    return payload.getCo2Level();
                }
                return null;
            }
            case HUMIDITY -> {
                if (data instanceof ClimateSensorAvro payload) {
                    return payload.getHumidity();
                }
                return null;
            }
            case LUMINOSITY -> {
                if (data instanceof LightSensorAvro payload) {
                    return payload.getLuminosity();
                }
                return null;
            }
        }
        return null;
    }
}
