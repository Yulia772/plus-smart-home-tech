package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.analyzer.model.ScenarioAction;
import ru.yandex.practicum.analyzer.model.ScenarioActionId;

import java.util.Collection;
import java.util.List;

public interface ScenarioActionRepository
        extends JpaRepository<ScenarioAction, ScenarioActionId> {

    @Modifying
    @Query("delete from ScenarioAction sa where sa.sensor.id = :sensorId")
    void deleteAllBySensorId(@Param("sensorId") String sensorId);

    @Modifying
    @Query("delete from ScenarioAction sa where sa.scenario.id = :scenarioId")
    void deleteAllByScenarioId(@Param("scenarioId") Long scenarioId);

    @Query("""
            select sa from ScenarioAction sa 
            join fetch sa.scenario
            join fetch sa.sensor
            join fetch sa.action
            where sa.scenario.id in :scenarioIds
    """)
    List<ScenarioAction> findByScenarioIdIn(@Param("scenarioIds") Collection<Long> scenarioIds);
}
