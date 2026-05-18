package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.analyzer.model.ScenarioAction;
import ru.yandex.practicum.analyzer.model.ScenarioActionId;

import java.util.Collection;
import java.util.List;

public interface ScenarioActionRepository
        extends JpaRepository<ScenarioAction, ScenarioActionId> {

    void deleteAllBySensor_Id(String sensorId);

    void deleteAllByScenario_Id(Long scenarioId);

    List<ScenarioAction> findByScenario_IdIn(Collection<Long> scenarioIds);
}
