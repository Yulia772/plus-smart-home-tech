package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.analyzer.model.ScenarioCondition;
import ru.yandex.practicum.analyzer.model.ScenarioConditionId;

import java.util.Collection;
import java.util.List;

public interface ScenarioConditionRepository
        extends JpaRepository<ScenarioCondition, ScenarioConditionId> {

    void deleteAllBySensor_Id(String sensorId);

    void deleteAllByScenario_Id(Long scenarioId);

    List<ScenarioCondition> findByScenario_IdIn(Collection<Long> scenarioIds);
}