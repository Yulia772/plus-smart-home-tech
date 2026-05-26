package ru.yandex.practicum.analyzer.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.mapper.HubRouterMapper;
import ru.yandex.practicum.analyzer.model.Action;
import ru.yandex.practicum.analyzer.model.ScenarioAction;
import ru.yandex.practicum.analyzer.model.Sensor;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubRouterClientImpl implements HubRouterClient {
    private final HubRouterMapper hubRouterMapper;

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterStub;

    @Override
    public void sendAction(String hubId, String scenarioName, Sensor sensor, Action action) {
        String sensorId = sensor.getId();
        Instant timestamp = Instant.now();

        DeviceActionProto actionProto = hubRouterMapper.toDeviceAction(sensorId, action);
        DeviceActionRequest request = hubRouterMapper.toDeviceActionRequest(hubId, scenarioName, actionProto, timestamp);
        hubRouterStub.handleDeviceAction(request);

        log.info("Действие отправлено в HubRouter: hubId={}, scenarioName={}, sensorId={}",
                hubId, scenarioName, sensorId);
    }

    @Override
    public void sendActions(String hubId, String scenarioName, List<ScenarioAction> scenarioActions) {
        for (ScenarioAction scenarioAction : scenarioActions) {
            sendAction(hubId,
                    scenarioName,
                    scenarioAction.getSensor(),
                    scenarioAction.getAction());
        }
}
}
