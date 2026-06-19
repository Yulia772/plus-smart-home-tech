package ru.yandex.practicum.analyzer.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.config.KafkaConfig;
import ru.yandex.practicum.analyzer.serialization.HubEventDeserializer;
import ru.yandex.practicum.analyzer.service.HubEventService;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    private final HubEventService hubEventService;
    private final KafkaConfig kafkaConfig;

    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);

    @Override
    public void run() {
        KafkaConsumer<String, HubEventAvro> consumer = new KafkaConsumer<>(getConsumerProperties());
        String hubTopic = kafkaConfig.getTopic().getHubs();

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(List.of(hubTopic));
            log.info("Analyzer подписался на топик событий хаба: {}", hubTopic);

            while (true) {
                ConsumerRecords<String, HubEventAvro> records =
                        consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    HubEventAvro event = record.value();
                    hubEventService.handle(event);
                }
                consumer.commitSync();
            }

        } catch (WakeupException ignored) {
            log.info("Останавливаем HubEventProcessor");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий хаба", e);
        } finally {
                log.info("Закрываем HubEventProcessor консьюмер");
                consumer.close();
        }
    }

    private Properties getConsumerProperties() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfig.getConsumer().getHubGroupId());

        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());

        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                HubEventDeserializer.class.getName());

        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return properties;
    }
}


