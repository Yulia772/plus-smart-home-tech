package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.analyzer.model.ScenarioCondition;
import ru.yandex.practicum.analyzer.model.ScenarioConditionId;

import java.util.Collection;
import java.util.List;

public interface ScenarioConditionRepository
        extends JpaRepository<ScenarioCondition, ScenarioConditionId> {

    @Modifying
    @Query("delete from ScenarioCondition sc where sc.sensor.id = :sensorId")
    void deleteAllBySensorId(@Param("sensorId") String sensorId);

    @Modifying
    @Query("delete from ScenarioCondition sc where sc.scenario.id = :scenarioId")
    void deleteAllByScenarioId(@Param("scenarioId") Long scenarioId);

    @Query("""
            select sc from ScenarioCondition sc 
            join fetch sc.scenario
            join fetch sc.sensor
            join fetch sc.condition
            where sc.scenario.id in :scenarioIds
    """)
    List<ScenarioCondition> findByScenarioIdIn(@Param("scenarioIds") Collection<Long> scenarioIds);
}