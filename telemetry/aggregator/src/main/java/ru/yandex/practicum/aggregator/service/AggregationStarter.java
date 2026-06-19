package ru.yandex.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.aggregator.config.KafkaConfig;
import ru.yandex.practicum.aggregator.serialization.SensorEventDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import telemetry.serialization.AvroSerializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

/**
 * Класс AggregationStarter, ответственный за запуск агрегации данных.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);

    private final SnapshotService snapshotService;
    private final KafkaConfig kafkaConfig;

    public void start() {

        KafkaConsumer<String, SensorEventAvro> consumer = new KafkaConsumer<>(getConsumerProperties());
        KafkaProducer<String, SensorsSnapshotAvro> producer = new KafkaProducer<>(getProducerProperties());

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            String sensorTopic = kafkaConfig.getTopic().getSensors();
            String snapshotsTopic = kafkaConfig.getTopic().getSnapshots();

            consumer.subscribe(List.of(sensorTopic));
            log.info("Aggregator подписался на topic: {}", sensorTopic);

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records =
                        consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    SensorEventAvro event = record.value();

                    snapshotService.updateState(event)
                            .ifPresent(snapshot -> producer.send(
                                    new ProducerRecord<>(
                                            snapshotsTopic,
                                            snapshot.getHubId(),
                                            snapshot)
                            ));

                }
                consumer.commitSync();
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {

            try {
                log.info("Сбрасываем буфер продюсера");
                producer.flush();

                log.info("Фиксируем смещение консьюмера");
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();

                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    private Properties getConsumerProperties() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfig.getConsumer().getGroupId());

        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());

        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                SensorEventDeserializer.class.getName());

        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return properties;
    }

    private Properties getProducerProperties() {
        Properties properties = new Properties();

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServers());

        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());

        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                AvroSerializer.class.getName());
        return properties;
    }
}
