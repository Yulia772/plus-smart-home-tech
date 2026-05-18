package ru.yandex.practicum.analyzer.service;

import ru.yandex.practicum.analyzer.model.Scenario;
import ru.yandex.practicum.analyzer.model.ScenarioCondition;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

import java.util.List;
import java.util.Map;

public interface ConditionEvaluator {
    boolean matches (List<ScenarioCondition> conditions, Map<String, SensorStateAvro> sensorState);
}
