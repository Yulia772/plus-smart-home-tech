package ru.yandex.practicum.analyzer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("analyzer.kafka")
public class KafkaConfig {
    private String bootstrapServers;
    private Topic topic;
    private Consumer consumer;

    @Getter
    @Setter
    public static class Topic {
        private String hubs;
        private String snapshots;
    }

    @Getter
    @Setter
    public static class Consumer {
        private String snapshotGroupId;
        private String hubGroupId;
    }
}
