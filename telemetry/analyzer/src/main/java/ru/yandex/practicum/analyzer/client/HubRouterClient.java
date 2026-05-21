package ru.yandex.practicum.analyzer.client;

import ru.yandex.practicum.analyzer.model.Action;
import ru.yandex.practicum.analyzer.model.ScenarioAction;
import ru.yandex.practicum.analyzer.model.Sensor;

import java.util.List;

public interface HubRouterClient {
    void sendAction (String hubId, String scenarioName, Sensor sensor, Action action);
    void sendActions (String hubId, String scenarioName, List<ScenarioAction> scenarioActions);
}
