package ru.yandex.practicum.analyzer.client;

import ru.yandex.practicum.analyzer.model.Action;
import ru.yandex.practicum.analyzer.model.Sensor;

public interface HubRouterClient {
    void sendAction (String hubId, String scenarioName, Sensor sensor, Action action);
}
