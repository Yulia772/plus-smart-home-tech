package ru.yandex.practicum.analyzer.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.mapper.ScenarioMapper;
import ru.yandex.practicum.analyzer.model.*;
import ru.yandex.practicum.analyzer.repository.*;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class ScenarioEventService {

    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;
    private final ScenarioMapper scenarioMapper;


    public void handleScenarioAdded(String hubId, ScenarioAddedEventAvro event) {
        if (scenarioRepository.findByHubIdAndName(hubId, event.getName()).isPresent()) {
            log.warn("Такой сценарий с hubId = {}, name = {} уже существует", hubId, event.getName());
            return;
        }
        Set<String> sensorIds = collectSensorIds(event);
        List<Sensor> sensors = sensorRepository.findAllByIdInAndHubId(sensorIds, hubId);

        Set<String> foundSensorIds = sensors.stream()
                .map(Sensor::getId)
                .collect(Collectors.toSet());

        Set<String> missing = new HashSet<>(sensorIds);
        missing.removeAll(foundSensorIds);

        if (!missing.isEmpty()) {
            log.warn("Невозможно сохранить сценарий name = {}, отсутствуют датчики ids = {}, hubId = {}",
                    event.getName(), missing, hubId);
            return;
        }

        Map<String, Sensor> sensorMap = sensors.stream()
                .collect(Collectors.toMap(Sensor::getId, sensor -> sensor));

        Scenario scenario = Scenario.builder()
                .hubId(hubId)
                .name(event.getName())
                .build();
        Scenario savedScenario = scenarioRepository.save(scenario);
        saveConditions(savedScenario, event.getConditions(), sensorMap);
        saveActions(savedScenario, event.getActions(), sensorMap);

        log.info("Сценарий name = {}, hubId = {} сохранен", event.getName(), hubId);
    }

    public void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro event) {
        Optional<Scenario> scenarioOptional = scenarioRepository.findByHubIdAndName(hubId, event.getName());

        if (scenarioOptional.isEmpty()) {
            log.warn("Сценарий c name = {}, hubId = {} не найден", event.getName(), hubId);
            return;
        }
        Scenario scenario = scenarioOptional.get();

        Long scenarioId = scenario.getId();

        scenarioConditionRepository.deleteAllByScenarioId(scenarioId);
        scenarioActionRepository.deleteAllByScenarioId(scenarioId);
        scenarioRepository.delete(scenario);

        log.info("Сценарий с name = {}, hubId = {} удален", event.getName(), hubId);
    }

    private Set<String> collectSensorIds(ScenarioAddedEventAvro event) {
        Set<String> sensorIds = new HashSet<>();

        for (ScenarioConditionAvro condition : event.getConditions()) {
            String sensorId = condition.getSensorId();
            sensorIds.add(sensorId);
        }

        for (DeviceActionAvro action : event.getActions()) {
            String sensorId = action.getSensorId();
            sensorIds.add(sensorId);
        }
        return sensorIds;
    }

    private void saveConditions(
            Scenario savedScenario,
            List<ScenarioConditionAvro> conditions,
            Map<String, Sensor> sensorMap
    ) {
        List<Condition> savedConditions = conditionRepository.saveAll(
                conditions.stream()
                        .map(scenarioMapper::toCondition)
                        .toList()
        );
        List<ScenarioCondition> scenarioConditions = new ArrayList<>();

        for (int i = 0; i < conditions.size(); i++) {
            ScenarioConditionAvro conditionAvro = conditions.get(i);
            Condition savedCondition = savedConditions.get(i);

            String sensorId = conditionAvro.getSensorId();
            Sensor sensor = sensorMap.get(sensorId);

            ScenarioCondition scenarioCondition = ScenarioCondition.builder()
                    .id(new ScenarioConditionId(
                            savedScenario.getId(),
                            sensorId,
                            savedCondition.getId()
                    ))
                    .scenario(savedScenario)
                    .sensor(sensor)
                    .condition(savedCondition)
                    .build();
            scenarioConditions.add(scenarioCondition);
        }
        scenarioConditionRepository.saveAll(scenarioConditions);
    }

    private void saveActions(
            Scenario savedScenario,
            List<DeviceActionAvro> actions,
            Map<String, Sensor> sensorMap
    ) {
        List<Action> savedActions = actionRepository.saveAll(
                actions.stream()
                        .map(scenarioMapper::toAction)
                        .toList()
        );

        List<ScenarioAction> scenarioActions = new ArrayList<>();

        for (int i = 0; i < actions.size(); i++) {
            DeviceActionAvro actionAvro = actions.get(i);
            Action savedAction = savedActions.get(i);

            String sensorId = actionAvro.getSensorId();
            Sensor sensor = sensorMap.get(sensorId);

            ScenarioAction scenarioAction = ScenarioAction.builder()
                    .id(new ScenarioActionId(
                            savedScenario.getId(),
                            sensorId,
                            savedAction.getId()
                    ))
                    .scenario(savedScenario)
                    .sensor(sensor)
                    .action(savedAction)
                    .build();
            scenarioActions.add(scenarioAction);
        }
        scenarioActionRepository.saveAll(scenarioActions);
    }
}


