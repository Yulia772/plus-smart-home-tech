package ru.yandex.practicum.analyzer.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.model.Action;
import ru.yandex.practicum.analyzer.model.Condition;
import ru.yandex.practicum.analyzer.model.enums.ActionType;
import ru.yandex.practicum.analyzer.model.enums.ConditionOperation;
import ru.yandex.practicum.analyzer.model.enums.ConditionType;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

@Component
public class ScenarioMapper {

    public Condition toCondition(ScenarioConditionAvro avro) {
        return Condition.builder()
                .type(ConditionType.valueOf(avro.getType().name()))
                .operation(ConditionOperation.valueOf(avro.getOperation().name()))
                .value(mapValue(avro.getValue()))
                .build();
    }

    public Action toAction(DeviceActionAvro avro) {
        return Action.builder()
                .type(ActionType.valueOf(avro.getType().name()))
                .value(avro.getValue())
                .build();
    }

    private Integer mapValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer integerValue) {
            return integerValue;
        }

        if (value instanceof Boolean booleanValue) {
            return booleanValue ? 1 : 0;
        }

        throw new IllegalArgumentException("Некорректный тип: " + value.getClass());
    }
}
