package ru.yandex.practicum.analyzer.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.client.HubRouterClient;
import ru.yandex.practicum.analyzer.model.*;
import ru.yandex.practicum.analyzer.repository.ScenarioActionRepository;
import ru.yandex.practicum.analyzer.repository.ScenarioConditionRepository;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class SnapshotServiceImpl implements SnapshotService {

    private final ScenarioRepository scenarioRepository;
    private final ConditionEvaluator conditionEvaluator;
    private final ScenarioActionRepository scenarioActionRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final HubRouterClient hubRouterClient;

    @Override
    public void handleSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        if (scenarios.isEmpty()) {
            log.info("Сценарии в hubId = {} отсутствуют", hubId);
            return;
        }
        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

        List<Long> scenarioIds = scenarios.stream()
                .map(Scenario::getId)
                .toList();

        List<ScenarioCondition> conditions =
                scenarioConditionRepository.findByScenario_IdIn(scenarioIds);
        Map<Long, List<ScenarioCondition>> conditionsByScenarioId = conditions.stream()
                .collect(Collectors.groupingBy(sc -> sc.getScenario().getId()));

        List<ScenarioAction> actions =
                scenarioActionRepository.findByScenario_IdIn(scenarioIds);
        Map<Long, List<ScenarioAction>> actionsByScenarioId = actions.stream()
                .collect(Collectors.groupingBy(sa -> sa.getScenario().getId()));

        for (Scenario scenario : scenarios) {
            Long scenarioId = scenario.getId();

            List<ScenarioCondition> scenarioConditions =
                    conditionsByScenarioId.getOrDefault(scenarioId, List.of());
            if (scenarioConditions.isEmpty()) {
                log.warn("Сценарий {} не содержит условий", scenario.getName());
                continue;
            }

            List<ScenarioAction> scenarioActions =
                    actionsByScenarioId.getOrDefault(scenarioId, List.of());
            if (scenarioActions.isEmpty()) {
                log.warn("Для сценария {} нет действий", scenario.getName());
                continue;
            }

            boolean matched = conditionEvaluator.matches(scenarioConditions, sensorsState);
            if (matched) {
                log.info("Сценарий {} для hubId={} выполнился", scenario.getName(), hubId);

                for (ScenarioAction scenarioAction : scenarioActions) {
                    Action action = scenarioAction.getAction();
                    Sensor sensor = scenarioAction.getSensor();
                    hubRouterClient.sendAction(hubId, scenario.getName(), sensor, action);
                }
            }
        }
    }
}
