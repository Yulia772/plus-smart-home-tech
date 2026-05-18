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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
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

        scenarioConditionRepository.deleteAllByScenario_Id(scenarioId);
        scenarioActionRepository.deleteAllByScenario_Id(scenarioId);
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
        for (ScenarioConditionAvro condition : conditions) {
            String sensorId = condition.getSensorId();
            Sensor sensor = sensorMap.get(sensorId);

            Condition jpaCondition = scenarioMapper.toCondition(condition);
            Condition savedCondition = conditionRepository.save(jpaCondition);

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
            scenarioConditionRepository.save(scenarioCondition);
        }
    }

    private void saveActions(
            Scenario savedScenario,
            List<DeviceActionAvro> actions,
            Map<String, Sensor> sensorMap
    ) {
        for (DeviceActionAvro action : actions) {
            String sensorId = action.getSensorId();
            Sensor sensor = sensorMap.get(sensorId);

            Action jpaAction = scenarioMapper.toAction(action);
            Action savedAction = actionRepository.save(jpaAction);

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
            scenarioActionRepository.save(scenarioAction);
        }
    }
}


