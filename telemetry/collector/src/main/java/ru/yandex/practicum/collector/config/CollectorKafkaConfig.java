package ru.yandex.practicum.collector.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("collector.kafka")
public class CollectorKafkaConfig {

    private Topic topic;

    @Getter
    @Setter
    public static class Topic {
        private String sensors;
        private String hubs;
    }
}
